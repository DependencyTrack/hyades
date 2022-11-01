package org.acme.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JacksonSerde<T> implements Serde<T> {

    private final JacksonSerializer<T> serializer;
    private final JacksonDeserializer<T> deserializer;

    public JacksonSerde(final Class<T> clazz) {
        this.serializer = new JacksonSerializer<>();
        this.deserializer = new JacksonDeserializer<>(clazz);
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
