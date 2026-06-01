#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEMO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
YAML="${DEMO_ROOT}/src/main/resources/application.yml"

# shellcheck source=load-crash-password.sh
source "${SCRIPT_DIR}/load-crash-password.sh"
# shellcheck source=crash-ssh-common.sh
source "${SCRIPT_DIR}/crash-ssh-common.sh"

CRASH_HOST="${CRASH_HOST:-127.0.0.1}"

_demo_auth_username() {
  awk '
    /^app:$/ { in_app=1; next }
    /^crash:$/ { in_app=0 }
    in_app && /^  auth:$/ { in_auth=1; next }
    in_app && in_auth && /^    username:/ {
      line = $0
      sub(/^    username:[[:space:]]*/, "", line)
      gsub(/^["'\'']|["'\'']$/, "", line)
      printf "%s", line
      exit
    }
    in_app && in_auth && /^  [a-z]/ && !/^    / { in_auth=0 }
  ' "$YAML" | tr -d '\r'
}

_demo_auth_password() {
  awk '
    /^app:$/ { in_app=1; next }
    /^crash:$/ { in_app=0 }
    in_app && /^  auth:$/ { in_auth=1; next }
    in_app && in_auth && /^    password:/ {
      line = $0
      sub(/^    password:[[:space:]]*/, "", line)
      gsub(/^["'\'']|["'\'']$/, "", line)
      printf "%s", line
      exit
    }
    in_app && in_auth && /^  [a-z]/ && !/^    / { in_auth=0 }
  ' "$YAML" | tr -d '\r'
}

_demo_crash_ssh_port() {
  local p
  p="$(awk '/^  ssh-port:/{print $2; exit}' "$YAML" | tr -d '\r')"
  printf '%s' "${p:-2000}"
}

if [[ ! -f "${YAML}" ]]; then
  echo "Missing ${YAML}" >&2
  exit 1
fi

connect_user="$(_demo_auth_username)"
connect_pass="$(_demo_auth_password)"
CRASH_SSH_PORT="$(_demo_crash_ssh_port)"

if [[ -z "${connect_user}" || -z "${connect_pass}" ]]; then
  echo "Could not read app.auth.username / app.auth.password from ${YAML}" >&2
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
  echo "Failed to obtain token (ssh exit ${ssh_ec}). Is the client demo running with CRaSH on port ${CRASH_SSH_PORT}?" >&2
  if [[ -s "${ssh_err}" ]]; then
    cat "${ssh_err}" >&2
  fi
  exit 1
fi

token=$(printf '%s\n' "${output}" | tr -d '\r' | grep -E '^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+' | tail -1)

if [[ -z "${token}" ]]; then
  echo "Empty token from tsa apiLogin." >&2
  [[ -s "${ssh_err}" ]] && cat "${ssh_err}" >&2
  exit 1
fi

printf '%s\n' "${token}"
