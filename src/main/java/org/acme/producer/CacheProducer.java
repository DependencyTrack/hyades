package org.acme.producer;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.acme.serde.CacheKeySerializer;
import org.acme.serde.ComponentAnalysisCacheSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Properties;


@ApplicationScoped
public class CacheProducer {

    private Producer<CacheKey, ComponentAnalysisCache> producer;

    @Inject
    ApplicationProperty applicationProperty;

    /*void onStart(@Observes StartupEvent event) {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationProperty.cacheProducerAppName());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, CacheKeySerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ComponentAnalysisCacheSerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, applicationProperty.enableIdempotence());
        properties.put(ProducerConfig.ACKS_CONFIG, applicationProperty.acksConfig());
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, applicationProperty.deliveryTimeout());
        properties.put(ProducerConfig.RETRIES_CONFIG, applicationProperty.retries());

        producer = new KafkaProducer<>(properties);
    }*/


    public void sendVulnCacheToKafka(CacheKey key, ComponentAnalysisCache cacheValue) {
        producer.send(new ProducerRecord<>(applicationProperty.topicComponentCache(), key, cacheValue));

    }

}
