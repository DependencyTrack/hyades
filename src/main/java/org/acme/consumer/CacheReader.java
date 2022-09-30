package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.runtime.StartupEvent;
import org.acme.model.CacheKey;
import org.acme.model.ComponentAnalysisCache;
import org.acme.serde.CacheKeyDeserializer;
import org.acme.serde.CacheKeySerializer;
import org.acme.serde.ComponentAnalysisCacheDeserializer;
import org.acme.serde.ComponentAnalysisCacheSerializer;
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
public class CacheReader {
    KafkaStreams streams;

    @ConfigProperty(name = "topic.component.cache")
    String cacheTopic;

    @ConfigProperty(name = "consumer.server")
    String server;

    @ConfigProperty(name = "consumer.offset")
    String offset;

    @ConfigProperty(name = "cache.global.ktable.topic")
    String cacheGKTable;

    @ConfigProperty(name = "component.cache.global.ktable.store.name")
    String cacheStoreName;
    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, cacheTopic);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapperSerde<ComponentAnalysisCache> componentSerde = new ObjectMapperSerde<>(ComponentAnalysisCache.class);
        ObjectMapperSerde<CacheKey> cacheSerde = new ObjectMapperSerde<>(CacheKey.class);
        GlobalKTable<CacheKey, ComponentAnalysisCache> componentCache = builder.globalTable(cacheTopic, Materialized.<CacheKey, ComponentAnalysisCache, KeyValueStore<Bytes, byte[]>>as(cacheStoreName)
                .withKeySerde(Serdes.serdeFrom(new CacheKeySerializer(), new CacheKeyDeserializer()))
                .withValueSerde(Serdes.serdeFrom(new ComponentAnalysisCacheSerializer(), new ComponentAnalysisCacheDeserializer())));
        streams = new KafkaStreams(builder.build(), props);
        streams.start();


    }
   public ComponentAnalysisCache getComponentCache(CacheKey key) {
        ComponentAnalysisCache cache = getCache().get(key);

        if(Objects.isNull(cache)){
            return null;
        }else
            return cache;
    }
    private ReadOnlyKeyValueStore<CacheKey, ComponentAnalysisCache> getCache() {
        while (true) {
            try {
                return streams.store(StoreQueryParameters.fromNameAndType(cacheStoreName, QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }

}