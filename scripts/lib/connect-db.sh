#!/usr/bin/env bash
set -euo pipefail

CONNECT_PG_HOST="${CONNECT_PG_HOST:-localhost}"
CONNECT_PG_PORT="${CONNECT_PG_PORT:-5432}"
CONNECT_PG_DB="${CONNECT_PG_DB:-tsa}"
CONNECT_PG_USER="${CONNECT_PG_USER:-admin}"
CONNECT_PG_PASSWORD="${CONNECT_PG_PASSWORD:-admin}"
DEMO_STATE_FILE="${DEMO_STATE_FILE:-${HOME}/.tsanet-client-demo/demo-state.env}"

_db_dir() {
  local dir="${BASH_SOURCE[0]:-$0}"
  cd "$(dirname "$dir")/../db" && pwd
}

_run_psql() {
  PGPASSWORD="${CONNECT_PG_PASSWORD}" psql \
    -h "${CONNECT_PG_HOST}" \
    -p "${CONNECT_PG_PORT}" \
    -U "${CONNECT_PG_USER}" \
    -d "${CONNECT_PG_DB}" \
    -t -A \
    "$@"
}

create_company() {
  local name="$1"
  local slug="$2"
  local email="$3"
  local result
  local db_dir
  db_dir="$(_db_dir)"

  result="$(_run_psql \
    -v company_name="${name}" \
    -v company_slug="${slug}" \
    -v user_email="${email}" \
    -f "${db_dir}/create-company.sql" | tail -1)"

  if [[ -z "${result}" ]]; then
    echo "Failed to create company ${slug}" >&2
    return 1
  fi

  IFS='|' read -r company_id document_id user_id _ slug _email <<< "${result}"
  mkdir -p "$(dirname "${DEMO_STATE_FILE}")"
  {
    echo "COMPANY_${slug//-/_}_ID=${company_id}"
    echo "COMPANY_${slug//-/_}_DOCUMENT_ID=${document_id}"
    echo "COMPANY_${slug//-/_}_USER_ID=${user_id}"
    echo "COMPANY_${slug//-/_}_EMAIL=${email}"
    echo "COMPANY_${slug//-/_}_SLUG=${slug}"
  } >> "${DEMO_STATE_FILE}"

  echo "Created company slug=${slug} companyId=${company_id} documentId=${document_id} userId=${user_id} email=${email}"
}

link_companies() {
  local slug_a="$1"
  local slug_b="$2"
  local result
  local db_dir
  db_dir="$(_db_dir)"

  result="$(_run_psql \
    -v slug_a="${slug_a}" \
    -v slug_b="${slug_b}" \
    -f "${db_dir}/link-companies.sql" | tail -1)"

  if [[ -z "${result}" ]]; then
    echo "Failed to link companies ${slug_a} and ${slug_b}" >&2
    return 1
  fi

  echo "Linked companies ${slug_a} <> ${slug_b} (communityId=${result%%|*})"
}

reset_demo_state() {
  : > "${DEMO_STATE_FILE}"
}

load_demo_state() {
  if [[ -f "${DEMO_STATE_FILE}" ]]; then
    # shellcheck disable=SC1090
    source "${DEMO_STATE_FILE}"
  fi
}

company_id_for_slug() {
  local slug="${1//-/_}"
  local variable="COMPANY_${slug}_ID"
  echo "${!variable:-}"
}

company_email_for_slug() {
  local slug="${1//-/_}"
  local variable="COMPANY_${slug}_EMAIL"
  echo "${!variable:-}"
}
