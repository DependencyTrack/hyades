package org.acme.consumer;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.acme.notification.NotificationRouter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.acme.model.* ;
import org.apache.kafka.streams.kstream.KStream;

import javax.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;
@ApplicationScoped

class NotificationTopologyBuilder {

    private final NotificationRouter router;

    public NotificationTopologyBuilder(final NotificationRouter notificationRouter){
        this.router = notificationRouter;
    }

    public Topology topology() {
        final var streamsBuilder = new StreamsBuilder();
        final var notificationSerde = new ObjectMapperSerde<>(Notification.class);
        final KStream<String, Notification> kStreams = streamsBuilder.stream(Pattern.compile("notification.*"),
                Consumed.with(Serdes.String(), notificationSerde));
        kStreams.foreach((key, notification) -> router.inform(notification));
        return streamsBuilder.build();
    }

}
