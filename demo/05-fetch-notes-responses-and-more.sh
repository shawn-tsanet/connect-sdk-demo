#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/tsa-client.sh
source "${SCRIPT_DIR}/../scripts/lib/tsa-client.sh"

echo "Fetching current user..."
me

echo
echo "Fetching webhook subscriptions..."
webhooks

echo
echo "Searching partners..."
partners "${1:-acme}"

echo
echo "Syncing collaboration requests, notes, and responses..."
sync_all

echo
echo "Stored notes:"
stored_notes

echo
echo "Stored responses:"
stored_responses
