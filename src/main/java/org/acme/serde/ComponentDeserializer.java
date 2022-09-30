package org.acme.serde;

import alpine.model.ConfigProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.Component;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class ComponentDeserializer implements Deserializer<Component> {

    @Override
    public Component deserialize(String topic, byte[] bytes) {
        try {
            return new ObjectMapper().readValue(bytes, Component.class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
