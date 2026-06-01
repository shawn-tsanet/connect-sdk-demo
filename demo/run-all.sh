#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

# Prerequisites (you start both yourself):
#   - TSANet Client Demo (HTTP :9090, CRaSH SSH :2000)
#   - Connect API on app.api.base-url (default :8080)
echo "Checking CRaSH at $(crash_endpoint) and Connect API at $(api_base_url)..."
ensure_ready

run_demo() {
  local script="$1"
  echo
  echo ">>> Running $(basename "$script")"
  bash "$script"
}

run_demo "${SCRIPT_DIR}/01-session-check.sh"
run_demo "${SCRIPT_DIR}/02-fetch-all-requests.sh"
run_demo "${SCRIPT_DIR}/03-fetch-requests-by-company.sh"
run_demo "${SCRIPT_DIR}/04-loop-companies.sh"
run_demo "${SCRIPT_DIR}/05-fetch-notes-responses-and-more.sh"
run_demo "${SCRIPT_DIR}/06-two-company-collaboration-flow.sh"

echo
echo "Demo completed."
