package org.acme.producer;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.event.VulnerabilityAnalysisEvent;
import org.acme.serde.VulnerabilityAnalysisEventSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Properties;

@ApplicationScoped
public class EventProducer {

    private Producer<String, Object> producer;
    @Inject
    ApplicationProperty applicationProperty;

    /*void onStart(@Observes StartupEvent event) {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationProperty.primaryEventProducer());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VulnerabilityAnalysisEventSerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, applicationProperty.enableIdempotence());
        properties.put(ProducerConfig.ACKS_CONFIG, applicationProperty.acksConfig());
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, applicationProperty.deliveryTimeout());
        properties.put(ProducerConfig.RETRIES_CONFIG, applicationProperty.retries());
        producer = new KafkaProducer<>(properties);
    }*/

    public void sendEventToKafka(VulnerabilityAnalysisEvent data) {
        producer.send(new ProducerRecord<>("event", "abc", data));
    }

}
