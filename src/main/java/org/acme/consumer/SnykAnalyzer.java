package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Blocking;
import org.acme.common.ApplicationProperty;
import org.acme.event.SnykAnalysisEvent;
import org.acme.model.Component;
import org.acme.serde.ArrayListDeserializer;
import org.acme.serde.ArrayListSerializer;
import org.acme.tasks.scanners.SnykAnalysisTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

//@ApplicationScoped
public class SnykAnalyzer {
    Logger logger = Logger.getLogger("poc");

    @Inject
    SnykAnalysisTask snykTask;
    @Inject
    SnykAnalysisEvent snykAnalysisEvent;

    Component component;

    KafkaStreams streams;

    @Inject
    ApplicationProperty applicationProperty;

    @Blocking
    void onStart(/*@Observes*/ StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationProperty.snykApplicationName());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, applicationProperty.consumerBatchSizeSnyk());
        final var streamsBuilder = new StreamsBuilder();
        Duration timeDifference = Duration.ofSeconds(applicationProperty.timeDifference());
        Duration gracePeriod = Duration.ofSeconds(applicationProperty.gracePeriod());
        TimeWindows tumblingWindow = TimeWindows.ofSizeAndGrace(timeDifference, gracePeriod);
        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, Component> kStreams = streamsBuilder.stream(applicationProperty.analysisTopic(), Consumed.with(Serdes.String(), componentSerde));
        KTable<Windowed<String>, ArrayList<Component>> componentArray = kStreams.groupByKey().windowedBy(tumblingWindow)
                .aggregate(ArrayList::new, (key, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        }, Materialized.<String, ArrayList<Component>, WindowStore<Bytes, byte[]>>
                                        as(applicationProperty.snykStoreName())
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.serdeFrom(new ArrayListSerializer(), new ArrayListDeserializer()))//Custom Serdes
                );
        componentArray.toStream().foreach((stringWindowed, components) -> {
            snykAnalysisEvent.setComponents(components);
            snykTask.inform(snykAnalysisEvent);
        });

        streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();
    }
}
