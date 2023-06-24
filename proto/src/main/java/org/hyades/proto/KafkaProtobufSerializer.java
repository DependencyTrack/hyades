package org.hyades.proto;

import com.google.protobuf.MessageLite;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaProtobufSerializer<T extends MessageLite> implements Serializer<T> {

    @Override
    public byte[] serialize(final String topic, final T data) {
        if (data == null) {
            return null;
        }

        try {
            return data.toByteArray();
        } catch (RuntimeException e) {
            throw new SerializationException(e);
        }
    }

}
