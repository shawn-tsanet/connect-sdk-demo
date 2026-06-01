#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

echo "Checking session via CRaSH ($(crash_endpoint)), Connect API: $(api_base_url)"
session
