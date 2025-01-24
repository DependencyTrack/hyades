#!/bin/bash

# Skript zum Überprüfen, ob der API-Server mit HTTP 200 antwortet
for i in {1..30}; do
  HTTP_CODE=$(curl --insecure -sL -w '%{http_code}' http://localhost:8081/health -o /dev/null)
  if [ "$HTTP_CODE" == "200" ]; then
    echo "API server is responding with HTTP 200!"
    exit 0
  fi
  echo "Waiting for API server to respond with HTTP 200... (Attempt $i)"
  sleep 10
done

echo "API server failed to respond in time!" && exit 1
