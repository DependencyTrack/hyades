#!/usr/bin/env bash

set -euo pipefail

if ! command -v trivy &>/dev/null; then
  echo "trivy was not found; ensure trivy is installed and re-run this script"
  exit 2
fi

mkdir -p "$(pwd)/fixtures/generated"

while read -r line < "$(pwd)/fixtures/images.txt"; do
    trivy image -f cyclonedx -o "$(pwd)/fixtures/generated/container_$(echo "$line" | sed 's/\//_/g' | sed 's/:/_/g').cdx.json" "$line"
done
