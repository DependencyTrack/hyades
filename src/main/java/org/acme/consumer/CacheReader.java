package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
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
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Objects;
import java.util.Properties;

//@ApplicationScoped
public class CacheReader {
    KafkaStreams streams;

    @Inject
    ApplicationProperty applicationProperty;

    void onStart(/*@Observes*/ StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationProperty.topicComponentCache());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        StreamsBuilder builder = new StreamsBuilder();
        GlobalKTable<CacheKey, ComponentAnalysisCache> componentCache = builder.globalTable(applicationProperty.topicComponentCache(), Materialized.<CacheKey, ComponentAnalysisCache, KeyValueStore<Bytes, byte[]>>as(applicationProperty.componentCacheStoreName())
                .withKeySerde(Serdes.serdeFrom(new CacheKeySerializer(), new CacheKeyDeserializer()))
                .withValueSerde(Serdes.serdeFrom(new ComponentAnalysisCacheSerializer(), new ComponentAnalysisCacheDeserializer())));
        streams = new KafkaStreams(builder.build(), props);
        streams.start();


    }

    public ComponentAnalysisCache getComponentCache(CacheKey key) {
        ComponentAnalysisCache cache = getCache().get(key);

        if (Objects.isNull(cache)) {
            return null;
        } else
            return cache;
    }

    private ReadOnlyKeyValueStore<CacheKey, ComponentAnalysisCache> getCache() {
        while (true) {
            try {
                return streams.store(StoreQueryParameters.fromNameAndType(applicationProperty.componentCacheStoreName(), QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }

}