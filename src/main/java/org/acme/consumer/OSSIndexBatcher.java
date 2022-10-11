package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.event.OssIndexAnalysisEvent;
import org.acme.model.Component;
import org.acme.serde.ArrayListDeserializer;
import org.acme.serde.ArrayListSerializer;
import org.acme.tasks.scanners.OssIndexAnalysisTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

@ApplicationScoped
public class OSSIndexBatcher {
    KafkaStreams streams;

    @Inject
    ApplicationProperty applicationProperty;

    @Inject
    OssIndexAnalysisEvent ossIndexAnalysisEvent;
    @Inject
    OssIndexAnalysisTask task;
    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "OSSConsumer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        final var streamsBuilder = new StreamsBuilder();

        Duration timeDifference = Duration.ofSeconds(2);
        Duration gracePeriod = Duration.ofMillis(500);

        List<Component> list = new ArrayList<>();

        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, Component> kStreams = streamsBuilder.stream(applicationProperty.analysisTopic(), Consumed.with(Serdes.String(), componentSerde));
        KTable<Windowed<String>, ArrayList<Component>> a = kStreams.groupByKey().windowedBy(SlidingWindows.withTimeDifferenceAndGrace(timeDifference, gracePeriod))
                .aggregate(() -> new ArrayList<Component>(), (k, v, aggr) -> { aggr.add(v); return aggr;}, Materialized.<String,ArrayList<Component>, WindowStore<Bytes, byte[]>>
                                as("userGroupedWindowStore")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.serdeFrom(new ArrayListSerializer(), new ArrayListDeserializer()))//Custom Serdes
                );
                a.toStream().foreach(new ForeachAction<Windowed<String>, ArrayList<Component>>() {
                    @Override
                    public void apply(Windowed<String> stringWindowed, ArrayList<Component> components) {
                        ossIndexAnalysisEvent.setComponents(components);
                        task.inform(ossIndexAnalysisEvent);
                    }
                });

        streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();


    }

}