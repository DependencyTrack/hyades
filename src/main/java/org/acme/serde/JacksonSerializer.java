package org.acme.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;


public class JacksonSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Override
    public byte[] serialize(final String topic, final T data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }

}
