#!/bin/bash
export KAFKA_HOME=/opt/kafka

echo "[init-kafka-topics] Waiting for Kafka to be ready..."
until nc -z localhost 9092; do sleep 1; done
echo "[init-kafka-topics] Kafka is up. Creating topics..."

$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.repo-meta-analysis.component --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.repo-meta-analysis.result --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.component --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.result --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.result.processed --partitions 1 --replication-factor 1 || true
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --if-not-exists --topic dtrack.vuln-analysis.scanner.result --partitions 1 --replication-factor 1 || true

echo "[init-kafka-topics] Topics created."
exit 0