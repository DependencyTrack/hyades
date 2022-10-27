package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
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
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.jboss.logging.Logger;

import java.util.Properties;

//@ApplicationScoped
public class VulnCacheReader {
    KafkaStreams streams;
    Logger logger = Logger.getLogger("poc");

    @Inject
    ApplicationProperty applicationProperty;

    void onStart(/*@Observes*/ StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationProperty.topicVulnCache());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        StreamsBuilder builder = new StreamsBuilder();
        GlobalKTable<Long, Vulnerability> vulnCache = builder.globalTable(applicationProperty.topicVulnCache(), Materialized.<Long, Vulnerability, KeyValueStore<Bytes, byte[]>>as(applicationProperty.vulnCacheStoreName())
                .withKeySerde(Serdes.Long())
                .withValueSerde(Serdes.serdeFrom(new VulnerabilitySerializer(), new VulnerabilityDeserializer())));
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
                return streams.store(StoreQueryParameters.fromNameAndType(applicationProperty.vulnCacheStoreName(), QueryableStoreTypes.keyValueStore()));
            } catch (InvalidStateStoreException e) {
                logger.error("There was an invalid state store exception: "+e.getMessage());
                logger.error(e.getStackTrace());
            }
        }
    }

}