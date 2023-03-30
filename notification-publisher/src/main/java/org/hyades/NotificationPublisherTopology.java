package org.hyades;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hyades.notification.NotificationRouter;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
class NotificationPublisherTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPublisherTopology.class);

    private final NotificationRouter router;

    @Inject
    public NotificationPublisherTopology(final NotificationRouter router) {
        this.router = router;
    }

    @Produces
    public Topology topology() {
        final var streamsBuilder = new StreamsBuilder();

        final var notificationSerde = new KafkaProtobufSerde<>(Notification.parser());

        final Optional<String> optionalPrefix = ConfigProvider.getConfig()
                .getOptionalValue("api.topic.prefix", String.class)
                .map(Pattern::quote);
        final var topicPattern = Pattern.compile(optionalPrefix.orElse("") + "dtrack\\.notification\\..+");

        streamsBuilder.stream(topicPattern,
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
