package org.acme.producer;

import org.acme.model.Cwe;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

@ApplicationScoped
public class CweDataProducer {

    private static final Producer<Integer, String> producer;


    static {
        final var properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "CweDataProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(properties);
    }


    public void sendCweToKafka( Cwe cweData) {
            producer.send(new ProducerRecord<>("cweValues", cweData.getCweId(), cweData.getName()));
    }

}
