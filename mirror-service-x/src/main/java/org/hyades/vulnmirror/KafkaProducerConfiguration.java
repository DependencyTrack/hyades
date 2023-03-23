package org.hyades.vulnmirror;

import io.smallrye.common.annotation.Identifier;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.HashMap;
import java.util.Map;

class KafkaProducerConfiguration {

    @Produces
    @ApplicationScoped
    Producer<String, byte[]> producer(@Identifier("default-kafka-broker") final Map<String, Object> config) {
        final var producerConfig = new HashMap<String, Object>();

        for (final Map.Entry<String, Object> entry : config.entrySet()) {
            if (ProducerConfig.configNames().contains(entry.getKey())) {
                producerConfig.put(entry.getKey(), entry.getValue());
            }
        }

        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new KafkaProducer<>(producerConfig);
    }

}
