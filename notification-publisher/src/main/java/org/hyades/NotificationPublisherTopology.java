package org.hyades;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.hyades.notification.NotificationRouter;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.regex.Pattern;

@ApplicationScoped
class NotificationPublisherTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPublisherTopology.class);
    private static final Pattern NOTIFICATION_TOPICS_PATTERN = Pattern.compile("dtrack\\.notification\\..+");

    private final NotificationRouter router;

    @Inject
    public NotificationPublisherTopology(final NotificationRouter router) {
        this.router = router;
    }

    @Produces
    public Topology topology() {
        final var streamsBuilder = new StreamsBuilder();

        final var notificationSerde = new KafkaProtobufSerde<>(Notification.parser());

        streamsBuilder.stream(NOTIFICATION_TOPICS_PATTERN,
                        Consumed.with(Serdes.String(), notificationSerde)
                                .withName("consume_from_notification_topics"))
                .foreach((key, notification) -> {
                            try {
                                router.inform(notification);
                            } catch (Exception e) {
                                LOGGER.error("Failed to publish notification", e);
                            }
                        },
                        Named.as("process_notification"));

        return streamsBuilder.build();
    }

}
