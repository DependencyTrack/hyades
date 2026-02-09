#!/bin/bash
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

echo "[apiserver] Waiting for Postgres..."
until pg_isready -h localhost -p 5432 -U postgres; do
  sleep 1
done

echo "[apiserver] Starting..."
exec java -Xmx2g -jar "/opt/hyades/apiserver/dependency-track-apiserver.jar"