#!/bin/bash
export KAFKA_HOME=/opt/kafka
KAFKA_CONFIG="$KAFKA_HOME/config/server.properties"

echo "[kafka] Starting Kafka with configuration: $KAFKA_CONFIG"

exec "$KAFKA_HOME/bin/kafka-server-start.sh" "$KAFKA_CONFIG"