package org.acme.consumer;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.runtime.StartupEvent;
import org.acme.common.ApplicationProperty;
import org.acme.event.OssIndexAnalysisEvent;
import org.acme.model.Component;
import org.acme.model.VulnerablityResult;
import org.acme.serde.ArrayListDeserializer;
import org.acme.serde.ArrayListSerializer;
import org.acme.serde.VulnerabilityResultDeserializer;
import org.acme.serde.VulnerabilityResultSerializer;
import org.acme.tasks.scanners.AnalyzerIdentity;
import org.acme.tasks.scanners.OssIndexAnalysisTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

//@ApplicationScoped
public class OSSIndexBatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OSSIndexBatcher.class);

    KafkaStreams streams;

    @Inject
    ApplicationProperty applicationProperty;


    @Inject
    OssIndexAnalysisTask task;

    void onStart(/*@Observes*/ StartupEvent event) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationProperty.ossApplicationName());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, applicationProperty.server());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, applicationProperty.consumerOffset());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        final var streamsBuilder = new StreamsBuilder();
        Duration timeDifference = Duration.ofSeconds(applicationProperty.timeDifference());
        Duration gracePeriod = Duration.ofSeconds(applicationProperty.gracePeriod());
        TimeWindows tumblingWindow = TimeWindows.ofSizeAndGrace(timeDifference, gracePeriod);
        ObjectMapperSerde<Component> componentSerde = new ObjectMapperSerde<>(Component.class);

        KStream<String, Component> kStreams = streamsBuilder.stream(applicationProperty.analysisTopic(), Consumed.with(Serdes.String(), componentSerde));
        kStreams.split()
                // For components that cannot be analyzed with OSS Index, report "no vulnerabilities"
                // immediately. This provides a faster feedback compared to waiting for the windowed
                // aggregation to complete first.
                .branch(
                        (k, v) -> v.isInternal() || v.getPurl() == null,
                        Branched.withConsumer(ks -> ks
                                .map((k, v) -> {
                                    final var result = new VulnerablityResult();
                                    result.setIdentity(AnalyzerIdentity.OSSINDEX_ANALYZER);
                                    result.setVulnerability(null);
                                    return new KeyValue<>(v.getUuid(), result);
                                })
                                .to(applicationProperty.topicVulnCacheResult(), Produced.with(Serdes.UUID(),
                                        Serdes.serdeFrom(new VulnerabilityResultSerializer(), new VulnerabilityResultDeserializer())))
                        )
                )
                .branch(
                        (k, v) -> true,
                        Branched.withConsumer(ks -> ks
                                .groupByKey().windowedBy(tumblingWindow)
                                .aggregate(ArrayList::new, (k, v, aggr) -> {
                                            aggr.add(v);
                                            return aggr;
                                        }, Materialized.<String, ArrayList<Component>, WindowStore<Bytes, byte[]>>
                                                        as(applicationProperty.ossStoreName())
                                                .withKeySerde(Serdes.String())
                                                .withValueSerde(Serdes.serdeFrom(new ArrayListSerializer(), new ArrayListDeserializer()))//Custom Serdes
                                )
                                .toStream()
                                .foreach((stringWindowed, components) -> {
                                    OssIndexAnalysisEvent ossIndexAnalysisEvent = new OssIndexAnalysisEvent();
                                    ossIndexAnalysisEvent.setComponents(components);
                                    task.inform(ossIndexAnalysisEvent);
                                })
                        )
                );

        streams = new KafkaStreams(streamsBuilder.build(), props);
        streams.start();
    }

}
