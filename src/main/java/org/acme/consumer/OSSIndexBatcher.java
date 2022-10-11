package org.acme.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.event.OssIndexAnalysisEvent;
import org.acme.model.Component;
import org.acme.tasks.scanners.OssIndexAnalysisTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, Component> kStreams = streamsBuilder.stream(applicationProperty.analysisTopic(), Consumed.with(Serdes.String(), componentSerde));
        kStreams.process(() -> new AbstractProcessor<String, Component>() {
            final LinkedBlockingQueue<Component> queue = new LinkedBlockingQueue<>(2);

            @Override
            public void init(ProcessorContext context) {
                super.init(context);
                context.schedule(Duration.of(applicationProperty.batchWaitTime(), ChronoUnit.MINUTES), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
                    processQueue();
                    context().commit();
                });

            }

            @Override
            public void process(String key, Component value) {
                queue.add(value);
                if (queue.remainingCapacity() == 0) {
                    processQueue();
                    context().commit();
                }
            }

            public void processQueue() {
                ArrayList<Component> collection = new ArrayList<>(queue);
                queue.clear();
                if (!collection.isEmpty()) {
                    ossIndexAnalysisEvent.setComponents(collection);
                    task.inform(ossIndexAnalysisEvent);
                }
            }

        });

        streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();


    }

}