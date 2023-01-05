package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.acme.common.KafkaTopic;
import org.acme.model.OsvAdvisory;
import org.acme.model.Vulnerability;
import org.acme.osv.OsvAnalyzer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.acme.commonutil.KafkaStreamsUtil.processorNameConsume;
import static org.acme.commonutil.KafkaStreamsUtil.processorNameProduce;

@ApplicationScoped
public class MirrorServiceTopology {

    private final OsvAnalyzer osvAnalyzer;

    @Inject
    public MirrorServiceTopology(final OsvAnalyzer osvAnalyzer) {
        this.osvAnalyzer = osvAnalyzer;
    }

    @Produces
    public Topology topology() {

        final var streamsBuilder = new StreamsBuilder();
        final var osvAdvisorySerde = new ObjectMapperSerde<>(OsvAdvisory.class);

        // OSV mirroring stream
        // (K,V) to be consumed as (event uuid, list of ecosystems)
        final KStream<String, String> mirrorOsv = streamsBuilder
                .stream(KafkaTopic.MIRROR_OSV.getName(), Consumed
                .with(Serdes.String(), Serdes.String())
                .withName(processorNameConsume(KafkaTopic.MIRROR_OSV)));
        mirrorOsv
                .flatMap((ecosystem, value) -> {
                    try {
                        return mirrorOsv(ecosystem);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, Named.as("mirror_osv_vulnerabilities"))
                .to(KafkaTopic.NEW_VULNERABILITY.getName(), Produced
                        .with(Serdes.String(), osvAdvisorySerde)
                        .withName(processorNameProduce(KafkaTopic.NEW_VULNERABILITY, "osv_vulnerability")));

        return streamsBuilder.build();
    }

    List<KeyValue<String, OsvAdvisory>> mirrorOsv(String ecosystem) throws IOException {
        return osvAnalyzer.performMirror(ecosystem).stream()
                .map(vulnerability -> KeyValue.pair(Vulnerability.Source.OSV.name() + "/" + vulnerability.getId(), vulnerability))
                .toList();
    }
}
