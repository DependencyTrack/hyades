package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.runtime.StartupEvent;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.acme.model.Vulnerability;
import org.acme.serde.*;
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
public class VulnCacheReader {
    KafkaStreams streams;

    @ConfigProperty(name = "topic.vuln.cache")
    String cacheTopic;

    @ConfigProperty(name = "consumer.server")
    String server;

    @ConfigProperty(name = "consumer.offset")
    String offset;

    @ConfigProperty(name = "cache.global.ktable.topic")
    String cacheGKTable;

    @ConfigProperty(name = "vuln.cache.global.ktable.store.name")
    String vulnCacheStoreName;
    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, cacheTopic);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        StreamsBuilder builder = new StreamsBuilder();
        //ObjectMapperSerde<Vulnerability> vulnerabilityObjectMapperSerde = new ObjectMapperSerde<>(Vulnerability.class);
        GlobalKTable<Long, Vulnerability> vulnCache = builder.globalTable(cacheTopic, Materialized.<Long, Vulnerability, KeyValueStore<Bytes, byte[]>>as(vulnCacheStoreName)
                .withKeySerde(Serdes.Long())
                .withValueSerde(Serdes.serdeFrom(new VulnerabilitySerializer(), new VulnerabilityDeserializer())));
        //KStream<CacheKey, ComponentAnalysisCache> componentCache = builder.stream("vuln-cache", Consumed.with(Serdes.serdeFrom(new CacheKeySerializer(), new CacheKeyDeserializer()), componentSerde));
        streams = new KafkaStreams(builder.build(), props);
        streams.start();


    }
    public Vulnerability getVulnCache(Long vulnId) {
        Vulnerability vuln = getCache().get(vulnId);
        return vuln;
    }
    private ReadOnlyKeyValueStore<Long, Vulnerability> getCache() {
        while (true) {
            try {
                return streams.store(StoreQueryParameters.fromNameAndType(vulnCacheStoreName, QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }

}