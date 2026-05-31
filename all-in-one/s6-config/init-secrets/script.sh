#!/bin/bash

echo "[init-secrets] Setting up secrets..."
mkdir -p /var/run/secrets
if [ ! -f /var/run/secrets/secret.key ]; then
    head -c 32 /dev/urandom > /var/run/secrets/secret.key
    chmod 644 /var/run/secrets/secret.key
fi

