#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=load-crash-password.sh
source "${SCRIPT_DIR}/load-crash-password.sh"
# shellcheck source=crash-ssh-common.sh
source "${SCRIPT_DIR}/crash-ssh-common.sh"

CRASH_HOST="${CRASH_HOST:-127.0.0.1}"
CRASH_SSH_PORT="${CRASH_SSH_PORT:-2000}"
CRASH_USER="${CRASH_USER:-crash}"

usage() {
  echo "Usage:" >&2
  echo "  CONNECT_PASSWORD=<connect-api-user-password> $0 <connect-username>" >&2
  echo "  $0 <connect-username> <connect-password>" >&2
  echo "" >&2
  echo "CONNECT_PASSWORD is the Connect backend /v1/login password (see app.auth.password in application.yml)." >&2
  echo "It is NOT the CRaSH SSH password (crash.auth-password, default crash) — that is read from tsanet-demo-crash-ssh.properties or CRASH_PASSWORD." >&2
  echo "CRASH_PASSWORD overrides crash.auth-password from tsanet-demo-crash-ssh.properties if set." >&2
  echo "" >&2
  echo "JWT is printed to stdout; details go to stderr." >&2
  exit 1
}

if [[ $# -lt 1 ]] || [[ $# -gt 2 ]]; then
  usage
fi

connect_user="$1"
connect_pass="${CONNECT_PASSWORD:-${2:-}}"

if [[ -z "${connect_pass}" ]]; then
  echo "Provide Connect API password via CONNECT_PASSWORD or as the second argument." >&2
  exit 1
fi

crash_ssh_require_tool || exit 1

crash_ssh_check_port "${CRASH_HOST}" "${CRASH_SSH_PORT}" || exit 1

remote_cmd=$(printf 'tsa apiLogin %q %q' "${connect_user}" "${connect_pass}")

ssh_err="$(mktemp)"
trap 'rm -f "${ssh_err}"' EXIT

set +e
output=$(
  crash_ssh_exec "${CRASH_PASSWORD}" \
    "${CRASH_SSH_OPTS_BATCH[@]}" \
    -p "${CRASH_SSH_PORT}" \
    "${CRASH_USER}@${CRASH_HOST}" \
    "${remote_cmd}" 2>"${ssh_err}"
)
ssh_ec=$?
set -e

if [[ "${ssh_ec}" -ne 0 ]]; then
  echo "SSH or remote command failed (exit ${ssh_ec})." >&2
  echo "Check: demo is running, crash.enabled is true, port ${CRASH_SSH_PORT}, user ${CRASH_USER}, CRaSH password (tsanet-demo-crash-ssh.properties)." >&2
  echo "Sanity check: ssh -p ${CRASH_SSH_PORT} ${CRASH_USER}@${CRASH_HOST}  (password: same as crash.auth-password)" >&2
  echo "If SSH works but login fails, CONNECT_PASSWORD must be the Connect API password for ${connect_user}." >&2
  if [[ -s "${ssh_err}" ]]; then
    echo "--- ssh stderr ---" >&2
    cat "${ssh_err}" >&2
  else
    echo "(ssh produced no stderr; try: ssh -vvv -p ${CRASH_SSH_PORT} ${CRASH_USER}@${CRASH_HOST})" >&2
  fi
  exit 1
fi

token=$(printf '%s\n' "${output}" | tr -d '\r' | grep -E '^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+' | tail -1)

if [[ -z "${token}" ]]; then
  echo "No token received from remote tsa apiLogin (empty stdout)." >&2
  if [[ -s "${ssh_err}" ]]; then
    echo "--- ssh stderr ---" >&2
    cat "${ssh_err}" >&2
  fi
  exit 1
fi

printf '%s\n' "${token}"
