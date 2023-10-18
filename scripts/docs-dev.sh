#!/usr/bin/env bash

set -euxo pipefail

SCRIPT_DIR="$(cd -P -- "$(dirname "$0")" && pwd -P)"
ROOT_DIR="$(cd -P -- "${SCRIPT_DIR}/.." && pwd -P)"

docker run --rm -it --name hyades-docs \
  -p "127.0.0.1:8000:8000" \
  -v "${ROOT_DIR}:/docs" \
  squidfunk/mkdocs-material
