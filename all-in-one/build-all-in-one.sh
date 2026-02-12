#!/bin/bash
set -e
docker build -f Dockerfile.all-in-one -t localhost/hyades-all-in-one:latest .