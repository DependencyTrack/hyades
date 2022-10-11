package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import alpine.common.util.BooleanUtil;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.event.VulnerabilityAnalysisEvent;
import org.acme.model.Component;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.KStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class PrimaryConsumer {

    @Inject
    ApplicationProperty applicationProperty;

    KafkaStreams streams;
    ConfigConsumer configConsumer;

    void onStart(@Observes StartupEvent event) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationProperty.primaryApplicationName());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapperSerde<VulnerabilityAnalysisEvent> vulnerabilityAnalysisEventSerde = new ObjectMapperSerde<>(
                VulnerabilityAnalysisEvent.class);
        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, VulnerabilityAnalysisEvent> kStreamsVulnTask = builder.stream(applicationProperty.primaryTopic(), Consumed.with(Serdes.String(), vulnerabilityAnalysisEventSerde)); //receiving the message on topic event
        //Setting the message key same as the name of component to send messages on different partitions of topic event-out
        KStream<String, Component> splittedStreams = kStreamsVulnTask.flatMap((s, vulnerabilityAnalysisEvent) -> {
            List<Component> components = vulnerabilityAnalysisEvent.getComponents();
            if (components.isEmpty()) {
                return Collections.emptyList();
            } else {
                ArrayList<KeyValue<String, Component>> componentList = new ArrayList<>();
                for (Component component : components) {
                    componentList.add(KeyValue.pair(component.getName(), component));
                }
                return componentList;
            }
        });
        splittedStreams.to(applicationProperty.analysisTopic(), (Produced<String, Component>) Produced.with(Serdes.String(), componentSerde));
        streams = new KafkaStreams(builder.build(), properties);
        streams.start();

    }

    public boolean isConsumerEnabled(String propertyName) {
        alpine.model.ConfigProperty configProperty = configConsumer.getConfigProperty(propertyName);
        return BooleanUtil.valueOf(configProperty.getPropertyValue());
    }

}