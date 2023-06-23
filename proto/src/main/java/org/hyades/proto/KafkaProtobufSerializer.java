package org.hyades.proto;

import com.google.protobuf.MessageLite;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KafkaProtobufSerializer<T extends MessageLite> implements Serializer<T> {

    @Override
    public byte[] serialize(final String topic, final T data) {
        if (data == null) {
            return null;
        }

        try (final var outStream = new ByteArrayOutputStream()) {
            data.writeTo(outStream);
            return outStream.toByteArray();
        } catch (IOException | RuntimeException e) {
            throw new SerializationException(e);
        }
    }

}
