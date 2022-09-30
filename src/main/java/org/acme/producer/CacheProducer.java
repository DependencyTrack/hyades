package org.acme.producer;

import org.acme.consumer.CacheReader;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.acme.model.Cwe;
import org.acme.serde.CacheKeySerializer;
import org.acme.serde.ComponentAnalysisCacheSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;

@ApplicationScoped
public class CacheProducer {

    private static final Producer<CacheKey, ComponentAnalysisCache> producer;


    static {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "CacheProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, CacheKeySerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ComponentAnalysisCacheSerializer.class.getName());
        producer = new KafkaProducer<>(properties);
    }


    public void sendVulnCacheToKafka(CacheKey key, ComponentAnalysisCache cacheValue) {
        producer.send(new ProducerRecord<>("component-cache", key, cacheValue));

    }

}
