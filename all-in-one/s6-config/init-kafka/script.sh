#!/bin/bash
export KAFKA_HOME=/opt/kafka

if [ -f "$KAFKA_HOME/config/kraft/server.properties" ]; then
    KRAFT_CONFIG="$KAFKA_HOME/config/kraft/server.properties"
else
    KRAFT_CONFIG="$KAFKA_HOME/config/server.properties"
fi

if [ ! -f "/tmp/kafka-logs/meta.properties" ]; then
    echo "[init-kafka] Formatting Kafka storage..."
    CLUSTER_ID=$($KAFKA_HOME/bin/kafka-storage.sh random-uuid)

    $KAFKA_HOME/bin/kafka-storage.sh format -t $CLUSTER_ID -c $KRAFT_CONFIG --standalone || exit 1
fi

echo "[init-kafka] Storage formatted successfully."
exit 0