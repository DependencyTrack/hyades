#!/bin/bash
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/dtrack
export QUARKUS_DATASOURCE_USERNAME=dtrack
export QUARKUS_DATASOURCE_PASSWORD=dtrack
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export DTRACK_INTERNAL_CLUSTER_ID=hyades-default-cluster

echo "[repo-meta-analyzer] Waiting for Postgres..."
until pg_isready -h localhost -p 5432 -U postgres; do
  sleep 1
done

echo "[repo-meta-analyzer] Waiting for database schema ..."
until gosu postgres psql -d dtrack -t -c "SELECT 1 FROM pg_tables WHERE tablename='CONFIGPROPERTY';" | grep -q 1; do
  sleep 5
done
echo "[repo-meta-analyzer] Schema is ready!"

echo "[repo-meta-analyzer] Starting..."
exec java  -jar "/opt/hyades/repo-meta-analyzer/quarkus-run.jar"