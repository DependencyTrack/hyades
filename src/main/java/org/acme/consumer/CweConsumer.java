package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.runtime.StartupEvent;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.acme.model.Cwe;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Objects;
import java.util.Properties;

@ApplicationScoped
public class CweConsumer {
    KafkaStreams streams;

    @ConfigProperty(name = "consumer.cwe.applicationId")
    String applicationId;

    @ConfigProperty(name = "consumer.server")
    String server;

    @ConfigProperty(name = "consumer.offset")
    String offset;

    @ConfigProperty(name = "cwe.global.ktable.topic")
    String cweTopic;

    @ConfigProperty(name = "cwe.global.ktable.store.name")
    String storeName;
    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapperSerde<Cwe> cweSerde = new ObjectMapperSerde<>(Cwe.class);
        GlobalKTable<Integer, String> cweTable = builder.globalTable(cweTopic, Materialized.<Integer, String, KeyValueStore<Bytes, byte[]>>as(storeName).withKeySerde(Serdes.Integer())
                .withValueSerde(Serdes.String()));
        streams = new KafkaStreams(builder.build(), props);
        streams.start();


    }
    public String getCweValues(int id) {
        String result = getCwe().get(id);

        return Objects.requireNonNullElse(result, null);
    }

    private ReadOnlyKeyValueStore<Integer, String> getCwe() {
        while (true) {
            try {
                return streams.store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }

}