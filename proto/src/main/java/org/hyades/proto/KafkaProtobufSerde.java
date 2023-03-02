package org.hyades.proto;

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class KafkaProtobufSerde<T extends MessageLite> implements Serde<T> {

    private final Serializer<T> serializer;
    private final Deserializer<T> deserializer;

    public KafkaProtobufSerde(final Parser<T> parser) {
        this.serializer = new KafkaProtobufSerializer<>();
        this.deserializer = new KafkaProtobufDeserializer<>(parser);
    }

    @Override
    public Serializer<T> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return deserializer;
    }

}
