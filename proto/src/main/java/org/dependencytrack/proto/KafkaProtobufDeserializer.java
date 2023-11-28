package org.dependencytrack.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class KafkaProtobufDeserializer<T extends MessageLite> implements Deserializer<T> {

    private final Parser<T> parser;

    public KafkaProtobufDeserializer(final Parser<T> parser) {
        this.parser = parser;
    }

    @Override
    public T deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return parser.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException(e);
        }
    }

}
