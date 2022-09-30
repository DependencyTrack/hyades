package org.acme.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class CacheKeySerializer implements Serializer<CacheKey> {

    private final ObjectMapper objectMapper = new ObjectMapper();/*.registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);*/

    @Override
    public byte[] serialize(String topic, CacheKey data) {
       /* try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(data);
            so.flush();
            serializedObject = bo.toString();
            return bo;
        } catch (Exception e) {
            throw new SerializationException(e)
        }*/
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }
}
