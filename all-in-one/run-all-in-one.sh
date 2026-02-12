#!/bin/bash
set -e

docker run --rm -it --name hyades-all-in-one -p 8080:9090 \
  -v hyades-postgres-data:/var/lib/postgresql/data:Z \
  -v hyades-kafka-data:/var/lib/kafka/data:Z \
  localhost/hyades-all-in-one:latest