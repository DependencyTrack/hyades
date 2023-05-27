package org.hyades.notification.serialization;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.proto.KafkaProtobufDeserializer;
import org.hyades.proto.notification.v1.Notification;

@RegisterForReflection
public class NotificationKafkaProtobufDeserializer extends KafkaProtobufDeserializer<Notification> {

    public NotificationKafkaProtobufDeserializer() {
        super(Notification.parser());
    }

}
