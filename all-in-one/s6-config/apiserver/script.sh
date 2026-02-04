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
until nc -z localhost 9092; do sleep 1; done
sleep 10
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.repo-meta-analysis.component --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.repo-meta-analysis.result --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.component --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.result --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.result.processed --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.scanner.result --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.new-vulnerability --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.new-vulnerable-dependency --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.analyzer --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.bom --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.configuration --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.datasource-mirroring --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.file-system --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.indexing-service --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.integration --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.project-audit-change --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.repository --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.user --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.notification.vex --partitions 1 --replication-factor 1 || true
exec java -Xmx2g -jar "$JAR_FILE"

