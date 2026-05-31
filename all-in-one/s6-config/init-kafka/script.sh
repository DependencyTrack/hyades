#!/bin/bash
set -e

export KAFKA_HOME="/opt/kafka"
KAFKA_CONFIG="$KAFKA_HOME/config/server.properties"
KAFKA_LOG_DIR="/var/lib/kafka/data"

# Check for existing metadata to avoid formatting the storage more than once.
if [ ! -f "$KAFKA_LOG_DIR/meta.properties" ]; then
    echo "[init-kafka] Storage at $KAFKA_LOG_DIR is not formatted. Formatting now..."

    CLUSTER_ID=$("$KAFKA_HOME/bin/kafka-storage.sh" random-uuid)
    "$KAFKA_HOME/bin/kafka-storage.sh" format -t "$CLUSTER_ID" -c "$KAFKA_CONFIG" --standalone

    echo "[init-kafka] Formatting complete with Cluster ID: $CLUSTER_ID"
else
    echo "[init-kafka] Storage already formatted. Metadata found in $KAFKA_LOG_DIR"
fi

exit 0