package org.acme.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.Component;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ArrayListSerializer implements Serializer<ArrayList<Component>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, ArrayList<Component> component) {
        try {
            return objectMapper.writeValueAsBytes(component);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }
}
