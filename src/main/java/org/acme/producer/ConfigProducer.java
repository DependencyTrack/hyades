package org.acme.producer;

import alpine.model.ConfigProperty;
import org.acme.serde.ConfigPropertySerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class ConfigProducer {

    private static final Producer<String, Object> producer;


    static {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "ConfigProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ConfigPropertySerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        producer = new KafkaProducer<>(properties);
    }


    public void sendConfigToKafka(List<ConfigProperty> list) {
        list.forEach(configProperty -> {
            producer.send(new ProducerRecord<>("configuration", configProperty.getPropertyName(), configProperty));
        });
    }
}
