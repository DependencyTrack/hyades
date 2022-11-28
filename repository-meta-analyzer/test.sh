#!/usr/bin/env bash

function printHelp() {
  echo "Emit repo-meta-analysis events for all components in a given BOM."
  echo ""
  echo "Usage: $0 <BOM>"
  echo "Options:"
  echo " -h  Print help"
  echo ""
  echo "The provided BOM must be in CycloneDX JSON format."
  echo ""
  echo "This script uses the Redpanda CLI rpk."
  echo "To install rpk via Homebrew:"
  echo "  brew install redpanda-data/tap/redpanda"
  echo ""
}

while getopts "h" opt; do
  case $opt in
    h)
      printHelp
      exit
      ;;
    *)
      ;;
  esac
done

if [ ! -f "$1" ]; then
  echo "No file provided or provided file does not exist"
  printHelp
  exit 1
elif [ "$(jq -r '.bomFormat' $1)" != "CycloneDX" ]; then
  echo "Provided file is not a CycloneDX BOM"
  exit 2
fi

for purl in $(jq -r '.components[].purl' $1); do
  uuid="$(uuidgen | awk '{print tolower($0)}')"
  echo "{\"uuid\":\"$uuid\",\"purl\":\"$purl\"}" | rpk \
    topic produce "dtrack.repo-meta-analysis.component" \
    --brokers 127.0.0.1:9092 \
    --key "$uuid" \
    --acks 0
done
