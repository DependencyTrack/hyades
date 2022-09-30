package org.acme.serde;

import alpine.model.ConfigProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class ConfigPropertySerializer implements Serializer<ConfigProperty> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, ConfigProperty configProperty) {
        try {
            return objectMapper.writeValueAsBytes(configProperty);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }
}
