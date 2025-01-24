#!/bin/bash

# Skript zum Überprüfen, ob DependencyTrack den Status "UP" hat
for i in {1..30}; do
  RESPONSE=$(curl -s http://localhost:8080/health)
  STATUS=$(echo "$RESPONSE" | jq -r '.status')

  if [ "$STATUS" == "UP" ]; then
    echo "DependencyTrack is ready!"
    exit 0
  fi

  echo "Waiting for DependencyTrack to be fully ready... (Attempt $i)"
  sleep 10
done

echo "DependencyTrack failed to start in time!" && exit 1
