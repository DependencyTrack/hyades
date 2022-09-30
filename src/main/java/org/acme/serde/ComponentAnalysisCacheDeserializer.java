package org.acme.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.ComponentAnalysisCache;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class ComponentAnalysisCacheDeserializer implements Deserializer<ComponentAnalysisCache> {
    @Override
    public ComponentAnalysisCache deserialize(String topic, byte[] data) {
        try {
            return new ObjectMapper().readValue(data, ComponentAnalysisCache.class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
