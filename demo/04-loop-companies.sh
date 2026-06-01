#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

COMPANY_IDS=(1 2 3)

echo "Looping over company IDs: ${COMPANY_IDS[*]}"
fetch_requests_for_companies "${COMPANY_IDS[@]}"

echo "All stored collaboration requests:"
stored_requests
