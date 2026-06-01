#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

echo "Fetching all collaboration requests from Connect API..."
fetch_requests

echo
echo "Reading stored collaboration requests from SQLite at $(db_path)..."
stored_requests
