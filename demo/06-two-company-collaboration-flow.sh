#!/usr/bin/env bash
# Requires: demo app (HTTP :9090, CRaSH :2000) + Connect API (default :8080).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"
# shellcheck source=../scripts/lib/connect-db.sh
source "${SCRIPT_DIR}/../scripts/lib/connect-db.sh"

DEMO_PASSWORD="${DEMO_PASSWORD:-T123456!}"
SLUG_A="${SLUG_A:-demo-alpha}"
SLUG_B="${SLUG_B:-demo-beta}"
EMAIL_A="${EMAIL_A:-alpha@demo.local}"
EMAIL_B="${EMAIL_B:-beta@demo.local}"
CASE_NUMBER="${CASE_NUMBER:-DEMO-$(date +%s)}"
REQUEST_SUMMARY="${REQUEST_SUMMARY:-Demo collaboration from Alpha to Beta}"

echo "=== Step 1: create first company ==="
reset_demo_state
create_company "Demo Alpha" "${SLUG_A}" "${EMAIL_A}"

echo
echo "=== Step 2: create second company ==="
create_company "Demo Beta" "${SLUG_B}" "${EMAIL_B}"

echo
echo "=== Step 3: link companies in Connect database ==="
link_companies "${SLUG_A}" "${SLUG_B}"

load_demo_state
COMPANY_A_ID="$(company_id_for_slug "${SLUG_A}")"
COMPANY_B_ID="$(company_id_for_slug "${SLUG_B}")"
EMAIL_A="$(company_email_for_slug "${SLUG_A}")"
EMAIL_B="$(company_email_for_slug "${SLUG_B}")"

echo
echo "=== Step 4: login as first company and inspect receiver form ==="
login "${EMAIL_A}" "${DEMO_PASSWORD}"
form "${COMPANY_B_ID}"

echo
echo "=== Step 5: create collaboration request for second company ==="
create_request "${COMPANY_B_ID}" "${CASE_NUMBER}" "${REQUEST_SUMMARY}" "Created by demo flow from ${SLUG_A} to ${SLUG_B}"

echo
echo "=== Step 6: logout and login as second company ==="
logout
login "${EMAIL_B}" "${DEMO_PASSWORD}"

echo
echo "=== Step 7: fetch all requests as second company ==="
REQUESTS_OUTPUT="$(fetch_requests)"
echo "${REQUESTS_OUTPUT}"

echo
echo "=== Step 8: verify created request is visible ==="
if echo "${REQUESTS_OUTPUT}" | grep -F "${REQUEST_SUMMARY}" >/dev/null; then
  echo "Found created collaboration request: ${REQUEST_SUMMARY}"
else
  echo "Created request not found in listing. Check Connect API auth/users for ${EMAIL_A} and ${EMAIL_B}." >&2
  exit 1
fi

echo
echo "Two-company collaboration demo completed."
