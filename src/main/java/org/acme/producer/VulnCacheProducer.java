package org.acme.producer;

import org.acme.model.Vulnerability;
import org.acme.serde.VulnerabilitySerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

@ApplicationScoped
public class VulnCacheProducer {
    @ConfigProperty(name = "topic.vuln.cache")
    String cacheTopic;
    private static final Producer<Long, Vulnerability> producer;


    static {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "VulnCacheProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VulnerabilitySerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60000);
        producer = new KafkaProducer<>(properties);
    }


    public void sendVulnCacheToKafka(Long vulnId, Vulnerability cacheValue) {
        producer.send(new ProducerRecord<>(cacheTopic, vulnId, cacheValue));

    }

}
