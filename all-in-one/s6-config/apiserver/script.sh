#!/bin/bash
export PGDATA=/var/lib/postgresql/data
export KAFKA_HOME=/opt/kafka
export PG_BIN_DIR=$(ls -d /usr/lib/postgresql/*/bin | head -n 1)

export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/dtrack
export QUARKUS_DATASOURCE_USERNAME=dtrack
export QUARKUS_DATASOURCE_PASSWORD=dtrack
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export ALPINE_DATABASE_MODE=production
export ALPINE_DATABASE_URL=jdbc:postgresql://localhost:5432/dtrack
export ALPINE_DATABASE_DRIVER=org.postgresql.Driver
export ALPINE_DATABASE_USERNAME=dtrack
export ALPINE_DATABASE_PASSWORD=dtrack
export ALPINE_SECRET_KEY_PATH=/var/run/secrets/secret.key
export ALPINE_METRICS_ENABLED=true

echo "[apiserver] Starting..."
JAR_FILE=$(find /opt/hyades/apiserver -maxdepth 1 -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
  echo "ERROR: No JAR found in /opt/hyades/apiserver"
  exit 1
fi
until nc -z localhost 5432; do sleep 1; done
exec java -Xmx2g -jar "$JAR_FILE"