#!/usr/bin/env bash

# This file is part of Dependency-Track.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) OWASP Foundation. All Rights Reserved.

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
