package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.acme.common.KafkaTopic;
import org.acme.model.OsvAdvisory;
import org.acme.osv.OsvAnalyzer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;

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

        final KStream<String, String> mirrorInitStream = streamsBuilder
                .stream(KafkaTopic.MIRROR_SERVICE.getName());

        if (osvAnalyzer.isEnabled()) {
            mirrorInitStream
                    .flatMap((key, value) -> mirrorOsv(), Named.as("mirror_osv_vulnerabilities"))
                    .to(KafkaTopic.MIRRORED_VULNERABILITY.getName(), Produced
                            .with(Serdes.String(), osvAdvisorySerde)
                            .withName(processorNameProduce(KafkaTopic.MIRRORED_VULNERABILITY, "osv_vulnerability")));
        }
        return streamsBuilder.build();
    }

    List<KeyValue<String, OsvAdvisory>> mirrorOsv() {
        return osvAnalyzer.performMirror().stream()
                .map(vulnerability -> KeyValue.pair(vulnerability.getId(), vulnerability))
                .toList();
    }
}
