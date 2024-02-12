#!/usr/bin/env bash

set -euox pipefail

SCRIPT_DIR="$(cd -P -- "$(dirname "$0")" && pwd -P)"
BOM_TESTDATA_DIR="$(cd -P -- "${SCRIPT_DIR}/../testdata/boms" && pwd -P)"

if ! command -v trivy &>/dev/null; then
  echo "trivy was not found; ensure trivy is installed and re-run this script"
  exit 2
fi

while read -r image_name; do
    trivy image -f cyclonedx -o "${BOM_TESTDATA_DIR}/generated/container_$(echo "${image_name}" | sed 's/\//_/g' | sed 's/:/_/g').cdx.json" "${image_name}"
done < "${BOM_TESTDATA_DIR}/generated/images.txt"
