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

set -euxo pipefail

SCRIPT_DIR="$(cd -P -- "$(dirname "$0")" && pwd -P)"
ROOT_DIR="$(cd -P -- "${SCRIPT_DIR}/.." && pwd -P)"

docker run -i --rm -u "$(id -u):$(id -g)" \
  -v "${ROOT_DIR}/docs/reference/schemas:/out" \
  -v "${ROOT_DIR}/proto/src/main/proto/org/dependencytrack/notification/v1:/protos" \
   pseudomuto/protoc-gen-doc --doc_opt=/out/notification.md.tmpl,notification.md

docker run -i --rm -u "$(id -u):$(id -g)" \
  -v "${ROOT_DIR}/docs/reference/schemas:/out" \
  -v "${ROOT_DIR}/proto/src/main/proto/org/dependencytrack/policy/v1:/protos" \
   pseudomuto/protoc-gen-doc --doc_opt=/out/policy.md.tmpl,policy.md