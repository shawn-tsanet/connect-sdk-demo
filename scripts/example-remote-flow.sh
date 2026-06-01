#!/usr/bin/env bash
# Same flow as demo/example-crash-flow.sh (CRaSH SSH → tsa commands).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/tsa-client.sh
source "${ROOT}/scripts/lib/tsa-client.sh"

ensure_ready
session
fetch_requests
