package org.acme.producer;

import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.acme.serde.CacheKeySerializer;
import org.acme.serde.ComponentAnalysisCacheSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;


@ApplicationScoped
public class CacheProducer {

    @ConfigProperty(name = "topic.component.cache")
    String cacheTopic;
    private static final Producer<CacheKey, ComponentAnalysisCache> producer;


    static {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "CacheProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, CacheKeySerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ComponentAnalysisCacheSerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60000);
        producer = new KafkaProducer<>(properties);
    }


    public void sendVulnCacheToKafka(CacheKey key, ComponentAnalysisCache cacheValue) {
        producer.send(new ProducerRecord<>(cacheTopic, key, cacheValue));

    }

}
