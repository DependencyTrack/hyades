package org.acme.producer;

import alpine.model.ConfigProperty;
import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.serde.ConfigPropertySerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class ConfigProducer {

    @Inject
    ApplicationProperty applicationProperty;

     private Producer<String, Object> producer;

    void onStart(@Observes StartupEvent event){
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationProperty.configProducerAppName());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ConfigPropertySerializer.class.getName());
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, applicationProperty.enableIdempotence());
        properties.put(ProducerConfig.ACKS_CONFIG, applicationProperty.acksConfig());
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, applicationProperty.deliveryTimeout());
        producer = new KafkaProducer<>(properties);
    }


    public void sendConfigToKafka(List<ConfigProperty> list) {
        list.forEach(configProperty -> {
            producer.send(new ProducerRecord<>("configuration", configProperty.getPropertyName(), configProperty));
        });
    }
}
