package org.acme.consumer;

import alpine.model.ConfigProperty;
import io.quarkus.runtime.StartupEvent;
import org.acme.Main;
import org.acme.event.VulnerabilityAnalysisEvent;
import org.acme.model.Component;
import org.acme.serde.ConfigPropertyDeserializer;
import org.acme.serde.ConfigPropertySerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.*;

@ApplicationScoped
public class ConfigConsumer {

    KafkaStreams streams;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "consumer.config.applicationId")
    String applicationId;
    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "consumer.server")
    String server;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "consumer.offset")
    String offset;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "config.global.ktable.topic")
    String configTopic;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "config.global.ktable.store.name")
    String storeName;

    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, ConfigProperty> configStream = builder.stream(configTopic, Consumed.with(Serdes.String(),Serdes.serdeFrom(new ConfigPropertySerializer(), new ConfigPropertyDeserializer()))); //receiving the message on topic event
        configStream.foreach(new ForeachAction<String, ConfigProperty>() {
            @Override
            public void apply(String configPropertyName, ConfigProperty configPropertyValue) {
                Main.configValues.put(configPropertyName, configPropertyValue);

            }
        });
        //Main.configValues.put(configStream.)
        //Splitting the incoming message into number of components
        //Setting the message key same as the name of component to send messages on different partitions of topic event-out

        streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }

    public List<ConfigProperty> getConfigProperties() {
        List<ConfigProperty> configProperties = new ArrayList<>(Main.configValues.values());

        return configProperties;
    }

    public ConfigProperty getConfigProperty(String propertyName) {

        if(Main.configValues.containsKey(propertyName))
            return Main.configValues.get(propertyName);
        else {
            return null;
        }
    }


}
