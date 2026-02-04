#!/bin/bash
export PGDATA=/var/lib/postgresql/data
export KAFKA_HOME=/opt/kafka
export PG_BIN_DIR=$(ls -d /usr/lib/postgresql/*/bin | head -n 1)

mkdir -p /var/run/secrets
if [ ! -f /var/run/secrets/secret.key ]; then
    head -c 32 /dev/urandom > /var/run/secrets/secret.key
    chmod 644 /var/run/secrets/secret.key
fi

