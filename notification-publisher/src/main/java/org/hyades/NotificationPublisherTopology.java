package org.hyades;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.hyades.model.Notification;
import org.hyades.notification.NotificationRouter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.regex.Pattern;

@ApplicationScoped
class NotificationPublisherTopology {

    private static final Pattern NOTIFICATION_TOPICS_PATTERN = Pattern.compile("dtrack\\.notification\\..+");

    private final NotificationRouter router;

    @Inject
    public NotificationPublisherTopology(final NotificationRouter router) {
        this.router = router;
    }

    @Produces
    public Topology topology() {
        final var streamsBuilder = new StreamsBuilder();
        final var notificationSerde = new ObjectMapperSerde<>(Notification.class);

        streamsBuilder.stream(NOTIFICATION_TOPICS_PATTERN,
                        Consumed.with(Serdes.String(), notificationSerde)
                                .withName("consume_from_notification_topics"))
                .foreach((key, notification) -> router.inform(notification),
                        Named.as("process_notification"));

        return streamsBuilder.build();
    }

}
