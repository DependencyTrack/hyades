package org.acme.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class EventDataDeserializer implements Deserializer<EventData> {


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public EventData deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        EventData data = null;
        try {
            data = mapper.readValue(bytes, EventData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public EventData deserialize(String topic, Headers headers, byte[] data) {
        return Deserializer.super.deserialize(topic, headers, data);
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
