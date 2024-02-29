#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -P -- "$(dirname "$0")" && pwd -P)"
BOM_TESTDATA_DIR="$(cd -P -- "${SCRIPT_DIR}/../testdata/boms" && pwd -P)"

while read -r image_name; do
    filename="container_$(echo "${image_name}" | sed 's/\//_/g' | sed 's/:/_/g').cdx.json"
    docker run -i --rm --user "$(id -u):$(id -g)" \
      -v "${HOME}/.cache/trivy:/tmp/trivy-cache" \
      -v "${BOM_TESTDATA_DIR}:/tmp/trivy-out" aquasec/trivy:0.49.1 \
      image --cache-dir /tmp/trivy-cache -f cyclonedx \
      -o "/tmp/trivy-out/generated/${filename}" \
      "${image_name}" </dev/null
done < "${BOM_TESTDATA_DIR}/generated/images.txt"
