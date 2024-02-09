#!/usr/bin/env bash

set -euo pipefail
shopt -s nullglob

SCRIPT_DIR="$(cd -P -- "$(dirname "$0")" && pwd -P)"
BOM_TESTDATA_DIR="$(cd -P -- "${SCRIPT_DIR}/../testdata/boms" && pwd -P)"
DEFAULT_API_URL='http://localhost:8080'
DEFAULT_USERNAME='admin'
DEFAULT_PASSWORD='admin123'

function print_help() {
  echo "Upload CycloneDX BOMs to Dependency-Track"
  echo ""
  echo "Usage: $0 [-a <API_URL>] [-u <USERNAME>] [-p <PASSWORD>] [-b <BOM_DIR>]"
  echo "Options:"
  echo " -a   Set Dependency-Track API URL (default: ${DEFAULT_API_URL})"
  echo " -u   Set username                 (default: ${DEFAULT_USERNAME})"
  echo " -p   Set password                 (default: ${DEFAULT_PASSWORD})"
  echo " -b   Set BOMs directory           (default: ${BOM_TESTDATA_DIR})"
  echo ""
  echo "Note: In order for BOM discovery to work, BOM files must be suffixed with .cdx.json"
  echo ""
}

function login() {
  api_url=$1
  username=$2
  password=$3

  echo "[+] authenticating as ${username}" 1>&2
  curl -s "${api_url}/api/v1/user/login" \
    -H 'Accept: */*' \
    -H 'Content-Type: application/x-www-form-urlencoded' \
    -d "username=${username}&password=${password}"
}

function upload_bom() {
  api_url=$1
  bearer_token=$2
  project_name=$3
  project_version=$4
  bom_file=$5

  echo "[+] uploading bom $(basename "${bom_file}") to project ${project_name}:${project_version}" 1>&2
  curl -s "${api_url}/api/v1/bom" \
    -H 'Content-Type: multipart/form-data' \
    -H "Authorization: Bearer ${bearer_token}" \
    -F 'autoCreate=true' \
    -F "projectName=${project_name}" \
    -F "projectVersion=${project_version}" \
    -F "bom=@${bom_file}"
}

while getopts ":h:a:u:p:b:" opt; do
  case $opt in
    a)
      api_url=$OPTARG
      ;;
    u)
      username=$OPTARG
      ;;
    p)
      password=$OPTARG
      ;;
    b)
      boms_dir=$OPTARG
      ;;
    h)
      print_help
      exit
      ;;
    *)
      print_help
      exit
      ;;
  esac
done

bearer_token="$(login "${api_url:-$DEFAULT_API_URL}" "${username:-$DEFAULT_USERNAME}" "${password:-$DEFAULT_PASSWORD}")"
if [[ $bearer_token != ey* ]]; then
  echo "[x] did not receive bearer token; go this instead: ${bearer_token}" 1>&2
  exit 1
fi

for bom_file in "${boms_dir:-$BOM_TESTDATA_DIR}"/**/*.cdx.json; do
  project_name="$(jq -r '.metadata.component.name' "${bom_file}")"
  project_version="$(jq -r '.metadata.component.version // empty' "${bom_file}")"
  random_uuid="$(uuidgen)"
  response="$(upload_bom "${api_url:-$DEFAULT_API_URL}" "${bearer_token}" "${project_name}" "${project_version:-$random_uuid}" "${bom_file}")"
  upload_token="$(echo "${response}" | jq -r '.token // empty' 2>/dev/null || true)"
  if [[ -z "${upload_token}" ]]; then
    echo "[!] upload failed; response: ${response}" 1>&2
  fi
done
