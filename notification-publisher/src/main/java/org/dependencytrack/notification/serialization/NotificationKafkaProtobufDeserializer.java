package org.dependencytrack.notification.serialization;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.dependencytrack.proto.KafkaProtobufDeserializer;
import org.dependencytrack.proto.notification.v1.Notification;

@RegisterForReflection
public class NotificationKafkaProtobufDeserializer extends KafkaProtobufDeserializer<Notification> {

    public NotificationKafkaProtobufDeserializer() {
        super(Notification.parser());
    }

}