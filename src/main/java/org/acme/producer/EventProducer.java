package org.acme.producer;

import org.acme.event.VulnerabilityAnalysisEvent;
import org.acme.serde.VulnerabilityAnalysisEventSerializer;
import org.acme.model.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

@ApplicationScoped
public class  EventProducer {

    private static final Producer<String, Object> producer;

    static {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "RequestSplitter");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VulnerabilityAnalysisEventSerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        producer = new KafkaProducer<>(properties);
    }

    public void sendEventToKafka(VulnerabilityAnalysisEvent data) {
        producer.send(new ProducerRecord<>("event", "abc", data));
    }

}
