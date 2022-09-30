package org.acme.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class CacheKeyDeserializer implements Deserializer<CacheKey> {
    @Override
    public CacheKey deserialize(String topic, byte[] data) {
        try {
            return new ObjectMapper().readValue(data, CacheKey.class);
        } catch (Exception e) {
            throw new SerializationException(e);
        }

        /*try {

            ByteArrayInputStream bi = new ByteArrayInputStream(data);
            ObjectInputStream si = new ObjectInputStream(bi);
            CacheKey obj = (CacheKey) si.readObject();
            return obj;
        } catch (Exception e) {
            throw new SerializationException(e);
        }*/
    }
}
