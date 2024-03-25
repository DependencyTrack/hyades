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

docker run --rm -it --name hyades-docs \
  -p "127.0.0.1:8000:8000" \
  -v "${ROOT_DIR}:/docs" \
  squidfunk/mkdocs-material
