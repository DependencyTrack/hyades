package org.acme.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.Component;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class ComponentSerializer implements Serializer<Component> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, Component component) {
        try {
            return objectMapper.writeValueAsBytes(component);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }
}
