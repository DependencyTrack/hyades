#!/bin/bash
set -e
docker build -f Dockerfile.all-in-one-base -t localhost/hyades-all-in-one-base:v1 .
docker build -f Dockerfile.all-in-one -t localhost/hyades-all-in-one:latest .