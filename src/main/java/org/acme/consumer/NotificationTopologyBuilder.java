package org.acme.consumer;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.acme.common.KafkaTopic;
import org.acme.model.Notification;
import org.acme.notification.NotificationRouter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import java.util.List;

class NotificationTopologyBuilder {

    public static void buildTopology(final StreamsBuilder streamsBuilder, final NotificationRouter router) {
        final var topics = List.of(
                KafkaTopic.ANALYZER_NOTIFICATION.getName(),
                KafkaTopic.BOM_CONSUMED_NOTIFICATION.getName(),
                KafkaTopic.BOM_PROCESSED_NOTIFICATION.getName(),
                KafkaTopic.CONFIGURATION_NOTIFICATION.getName(),
                KafkaTopic.DATASOURCE_MIRRORING_NOTIFICATION.getName(),
                KafkaTopic.FILE_SYSTEM_NOTIFICATION.getName(),
                KafkaTopic.INDEXING_SERVICE_NOTIFICATION.getName(),
                KafkaTopic.INTEGRATION_NOTIFICATION.getName(),
                KafkaTopic.NEW_VULNERABILITY_NOTIFICATION.getName(),
                KafkaTopic.NEW_VULNERABLE_DEPENDENCY_NOTIFICATION.getName(),
                KafkaTopic.POLICY_VIOLATION_NOTIFICATION.getName(),
                KafkaTopic.PROJECT_AUDIT_CHANGE_NOTIFICATION.getName(),
                KafkaTopic.REPOSITORY_NOTIFICATION.getName(),
                KafkaTopic.VEX_CONSUMED_NOTIFICATION.getName(),
                KafkaTopic.VEX_PROCESSED_NOTIFICATION.getName()
        );

        final var notificationSerde = new ObjectMapperSerde<>(Notification.class);
        final KStream<String, Notification> kStreams = streamsBuilder.stream(topics,
                Consumed.with(Serdes.String(), notificationSerde));
        kStreams.foreach((key, notification) -> router.inform(notification));
    }

}
