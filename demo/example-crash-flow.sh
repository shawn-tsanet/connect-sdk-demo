#!/usr/bin/env bash
# Minimal CRaSH-driven demo (same stack as run-all.sh).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

ensure_ready
session
fetch_requests
me
