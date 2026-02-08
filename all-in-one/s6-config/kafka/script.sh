#!/bin/bash
export KAFKA_HOME=/opt/kafka

if [ -f "$KAFKA_HOME/config/kraft/server.properties" ]; then
    KRAFT_CONFIG="$KAFKA_HOME/config/kraft/server.properties"
elif [ -f "$KAFKA_HOME/config/kraft/reconfig-server.properties" ]; then
    KRAFT_CONFIG="$KAFKA_HOME/config/kraft/reconfig-server.properties"
elif [ -f "$KAFKA_HOME/config/server.properties" ]; then
    KRAFT_CONFIG="$KAFKA_HOME/config/server.properties"
else
    echo "ERROR: KRaft configuration file not found!"
    echo "Listing $KAFKA_HOME/config:"
    ls -R $KAFKA_HOME/config
    exit 1
fi

echo "[kafka] Starting Kafka with configuration: $KRAFT_CONFIG"

exec $KAFKA_HOME/bin/kafka-server-start.sh $KRAFT_CONFIG