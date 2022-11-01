package org.acme.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class JacksonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Class<T> clazz;

    public JacksonDeserializer(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(final String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
