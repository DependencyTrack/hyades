#!/usr/bin/env zsh

plumber write kafka \
  --address localhost:9092 \
  --topics component-analysis \
  --key 2bda3139-647f-47ff-9c35-ffc16adf3076 \
  --input-file input.json \
  --input-as-json-array
