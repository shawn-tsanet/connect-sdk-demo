#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

COMPANY_ID="${1:-1}"

echo "Fetching collaboration requests for companyId=${COMPANY_ID}..."
fetch_requests "$COMPANY_ID"

echo
echo "Stored requests for companyId=${COMPANY_ID}:"
stored_requests "$COMPANY_ID"
