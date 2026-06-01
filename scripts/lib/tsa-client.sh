#!/usr/bin/env bash
# Demo shell library — drives the running app via CRaSH over SSH (port 2000).
#
# Prerequisites (you start these yourself; scripts do not start them):
#   1. TSANet Client Demo (HTTP :9090, CRaSH SSH :2000)
#   2. Connect API at app.api.base-url (default http://localhost:8080)
#
# Each function runs one CRaSH command:  ssh crash@host -p 2000  →  tsa <command>
set -euo pipefail

_lib_dir() {
  cd "$(dirname "${BASH_SOURCE[0]}")" && pwd
}

_scripts_dir() {
  cd "$(_lib_dir)/.." && pwd
}

_project_root() {
  if [[ -n "${TSA_ROOT:-}" ]]; then
    echo "${TSA_ROOT}"
    return
  fi
  TSA_ROOT="$(cd "$(_scripts_dir)/.." && pwd)"
  echo "${TSA_ROOT}"
}

_app_yml() {
  echo "$(_project_root)/src/main/resources/application.yml"
}

_api_base_url() {
  if [[ -n "${CONNECT_API_BASE_URL:-}" ]]; then
    echo "${CONNECT_API_BASE_URL}"
    return
  fi
  local url
  url="$(awk '/base-url:/{print $2; exit}' "$(_app_yml)" | tr -d '"\r' 2>/dev/null || true)"
  echo "${url:-http://localhost:8080}"
}

_crash_ssh_port() {
  if [[ -n "${CRASH_SSH_PORT:-}" ]]; then
    echo "${CRASH_SSH_PORT}"
    return
  fi
  local p
  p="$(awk '/^  ssh-port:/{print $2; exit}' "$(_app_yml)" | tr -d '\r' 2>/dev/null || true)"
  echo "${p:-2000}"
}

_auth_username() {
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
  ' "$(_app_yml)" | tr -d '\r'
}

_auth_password() {
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
  ' "$(_app_yml)" | tr -d '\r'
}

_init_crash_ssh() {
  # shellcheck source=../load-crash-password.sh
  source "$(_scripts_dir)/load-crash-password.sh"
  # shellcheck source=../crash-ssh-common.sh
  source "$(_scripts_dir)/crash-ssh-common.sh"
  CRASH_HOST="${CRASH_HOST:-127.0.0.1}"
  CRASH_SSH_PORT="$(_crash_ssh_port)"
  CRASH_USER="${CRASH_USER:-crash}"
  export CRASH_SSH_OPTS=("${CRASH_SSH_OPTS_BATCH[@]}")
}

# Run a CRaSH command on the tsa namespace (e.g. crash_exec session → tsa session).
crash_exec() {
  _init_crash_ssh
  crash_ssh_require_tool || return 1
  crash_ssh_exec "${CRASH_PASSWORD}" \
    -p "${CRASH_SSH_PORT}" \
    "${CRASH_USER}@${CRASH_HOST}" \
    tsa "$@"
}

check_crash_shell() {
  _init_crash_ssh
  if ! crash_ssh_check_port "${CRASH_HOST}" "${CRASH_SSH_PORT}"; then
    exit 1
  fi
  local probe_out probe_ec=0
  probe_out="$(crash_exec help 2>&1)" || probe_ec=$?
  if [[ ${probe_ec} -ne 0 ]] || echo "${probe_out}" | grep -qi 'permission denied'; then
    echo "CRaSH SSH auth failed at ${CRASH_HOST}:${CRASH_SSH_PORT} (user=${CRASH_USER})." >&2
    if [[ -n "${probe_out}" ]]; then
      echo "${probe_out}" >&2
    fi
    echo "Set CRASH_PASSWORD to match crash.auth-password in the running app (default: crash)." >&2
    exit 1
  fi
  echo "CRaSH shell is up at ${CRASH_HOST}:${CRASH_SSH_PORT}" >&2
}

check_connect_api() {
  local base health_url
  base="$(_api_base_url)"
  health_url="${base%/}/v1/health"
  if curl -sf "${health_url}" >/dev/null 2>&1; then
    echo "Connect API is up at ${base}" >&2
    return 0
  fi
  echo "Connect API is not reachable at ${health_url}" >&2
  echo "Start Connect API on ${base} (app.api.base-url), then re-run." >&2
  exit 1
}

ensure_ready() {
  check_crash_shell
  check_connect_api

  local session_out
  session_out="$(crash_exec session 2>&1)" || true
  if echo "${session_out}" | grep -q 'authorized: true'; then
    echo "Connect API session is authorized in the running app." >&2
    return 0
  fi

  local user pass
  user="$(_auth_username)"
  pass="$(_auth_password)"
  if [[ -z "${user}" || -z "${pass}" ]]; then
    echo "Not authorized. Set app.auth in application.yml or run: login USER PASS" >&2
    exit 1
  fi

  echo "Logging in via CRaSH as ${user}..." >&2
  local login_out login_ec=0
  login_out="$(crash_exec login "${user}" "${pass}" 2>&1)" || login_ec=$?
  if [[ -n "${login_out}" ]]; then
    echo "${login_out}" >&2
  fi
  if [[ ${login_ec} -ne 0 ]]; then
    echo "CRaSH login command failed (exit ${login_ec}). Check CRaSH SSH credentials (crash/crash by default)." >&2
    exit 1
  fi

  session_out="$(crash_exec session 2>&1)" || true
  if ! echo "${session_out}" | grep -q 'authorized: true'; then
    echo "Connect API login via CRaSH did not authorize the session." >&2
    if [[ -n "${session_out}" ]]; then
      echo "${session_out}" >&2
    fi
    echo "Verify app.auth credentials against Connect API at $(_api_base_url)." >&2
    exit 1
  fi
  echo "Login succeeded (session kept in running app)." >&2
}

session() {
  crash_exec session
}

login() {
  crash_exec login "$1" "$2"
}

logout() {
  crash_exec logout
}

fetch_requests() {
  if [[ $# -eq 0 ]]; then
    crash_exec requests
    return
  fi
  crash_exec requestsForCompany "$1"
}

stored_requests() {
  if [[ $# -eq 0 ]]; then
    crash_exec storedRequests
    return
  fi
  crash_exec storedRequestsForCompany "$1"
}

fetch_notes() {
  if [[ $# -eq 0 ]]; then
    crash_exec notesAll
    return
  fi
  crash_exec notesForToken "$1"
}

stored_notes() {
  if [[ $# -eq 0 ]]; then
    crash_exec storedNotes
    return
  fi
  crash_exec storedNotesForToken "$1"
}

fetch_responses() {
  if [[ $# -eq 0 ]]; then
    crash_exec responsesAll
    return
  fi
  crash_exec responsesForToken "$1"
}

stored_responses() {
  if [[ $# -eq 0 ]]; then
    crash_exec storedResponses
    return
  fi
  crash_exec storedResponsesForToken "$1"
}

sync_all() {
  crash_exec sync
}

me() {
  crash_exec me
}

webhooks() {
  crash_exec webhooks
}

partners() {
  crash_exec partners "$1"
}

fetch_requests_for_companies() {
  local company_id
  for company_id in "$@"; do
    echo "=== Fetching collaboration requests for companyId=${company_id} ==="
    fetch_requests "$company_id"
    echo
  done
}

form() {
  crash_exec form "$1"
}

create_request() {
  crash_exec createRequest "$1" "$2" "$3" "$4"
}

db_path() {
  echo "${HOME}/.tsanet-client-demo/data.db"
}

api_base_url() {
  _api_base_url
}

crash_endpoint() {
  echo "${CRASH_HOST:-127.0.0.1}:$(_crash_ssh_port)"
}
