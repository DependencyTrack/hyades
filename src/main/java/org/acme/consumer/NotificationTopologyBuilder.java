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
import java.util.regex.Pattern;

class NotificationTopologyBuilder {

    public static void buildTopology(final StreamsBuilder streamsBuilder, final NotificationRouter router) {
        final var notificationSerde = new ObjectMapperSerde<>(Notification.class);
        final KStream<String, Notification> kStreams = streamsBuilder.stream(Pattern.compile("notification.*"),
                Consumed.with(Serdes.String(), notificationSerde));
        kStreams.foreach((key, notification) -> router.inform(notification));
    }

}
