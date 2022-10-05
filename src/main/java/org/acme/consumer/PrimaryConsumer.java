package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import alpine.common.util.BooleanUtil;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

import io.quarkus.runtime.StartupEvent;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class PrimaryConsumer {

    @ConfigProperty(name = "consumer.primary.applicationId")
    String applicationId;

    @ConfigProperty(name = "consumer.server")
    String server;

    @ConfigProperty(name = "consumer.offset")
    String offset;

    @ConfigProperty(name = "topic.event")
    String eventTopic;

    @ConfigProperty(name = "topic.in.primary")
    String inputTopic;
    KafkaStreams streams;
    ConfigConsumer configConsumer;

    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);


        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapperSerde<VulnerabilityAnalysisEvent> vulnerabilityAnalysisEventSerde = new ObjectMapperSerde<>(
                VulnerabilityAnalysisEvent.class);
        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, VulnerabilityAnalysisEvent> kStreamsVulnTask = builder.stream(inputTopic, Consumed.with(Serdes.String(), vulnerabilityAnalysisEventSerde)); //receiving the message on topic event
        //Setting the message key same as the name of component to send messages on different partitions of topic event-out
        // KStream<String, Component> splittedStreams = kStreamsVulnTask.flatMapValues(value -> value.getComponents()).selectKey((key, value) -> value.getName());
        KStream<String, Component> splittedStreams = kStreamsVulnTask.flatMap((s, vulnerabilityAnalysisEvent) -> {
            List<Component> components = vulnerabilityAnalysisEvent.getComponents();
            for (Component component : components) {
                return Collections.singletonList(KeyValue.pair(component.getName(), component));
            }
            return Collections.emptyList();
        });
        splittedStreams.to(eventTopic, (Produced<String, Component>) Produced.with(Serdes.String(), componentSerde));
        streams = new KafkaStreams(builder.build(), props);
        streams.start();

    }

    public boolean isConsumerEnabled(String propertyName) {
        alpine.model.ConfigProperty configProperty = configConsumer.getConfigProperty(propertyName);
        return BooleanUtil.valueOf(configProperty.getPropertyValue());
    }

}