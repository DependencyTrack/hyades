package org.acme.serde;

import alpine.model.ConfigProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.ComponentAnalysisCache;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

public class ConfigPropertyDeserializer implements Deserializer<ConfigProperty> {

    @Override
    public ConfigProperty deserialize(String topic, byte[] bytes) {
        try {
            return new ObjectMapper().readValue(bytes, ConfigProperty.class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
