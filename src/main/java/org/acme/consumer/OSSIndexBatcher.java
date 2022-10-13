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
import org.apache.kafka.streams.state.WindowStore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

@ApplicationScoped
public class OSSIndexBatcher {
    KafkaStreams streams;

    @Inject
    ApplicationProperty applicationProperty;


    @Inject
    OssIndexAnalysisTask task;

    void onStart(@Observes StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationProperty.ossApplicationName());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        final var streamsBuilder = new StreamsBuilder();

        Duration timeDifference = Duration.ofSeconds(5);
        TimeWindows tumblingWindow = TimeWindows.of(timeDifference);


        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, Component> kStreams = streamsBuilder.stream(applicationProperty.analysisTopic(), Consumed.with(Serdes.String(), componentSerde));
        KTable<Windowed<String>, ArrayList<Component>> a = kStreams.groupByKey().windowedBy(tumblingWindow)
                .aggregate(ArrayList::new, (k, v, aggr) -> {
                            aggr.add(v);
                            return aggr;
                        }, Materialized.<String, ArrayList<Component>, WindowStore<Bytes, byte[]>>
                                        as(applicationProperty.ossStoreName())
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.serdeFrom(new ArrayListSerializer(), new ArrayListDeserializer()))//Custom Serdes
                );
        a.toStream().foreach((stringWindowed, components) -> {
            OssIndexAnalysisEvent ossIndexAnalysisEvent = new OssIndexAnalysisEvent();
            ossIndexAnalysisEvent.setComponents(components);
            task.inform(ossIndexAnalysisEvent);
        });

        streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();
    }

}
