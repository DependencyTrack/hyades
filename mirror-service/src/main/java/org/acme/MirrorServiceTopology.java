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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.acme.commonutil.KafkaStreamsUtil.processorNameProduce;

@ApplicationScoped
public class MirrorServiceTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(MirrorServiceTopology.class);

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
                    .map((key, value) -> mirrorOsv(), Named.as("mirror_osv_vulnerability"))
                    .to(KafkaTopic.MIRRORED_VULNERABILITY.getName(), Produced
                            .with(Serdes.String(), osvAdvisorySerde)
                            .withName(processorNameProduce(KafkaTopic.MIRRORED_VULNERABILITY, "osv_vulnerability")));
        }
        return streamsBuilder.build();
    }

    KeyValue<String, OsvAdvisory> mirrorOsv() {
        // TODO: instead of returning, performMirror should publish directly for each vuln
        OsvAdvisory osvAdvisory = osvAnalyzer.performMirror();
        return KeyValue.pair(osvAdvisory.getId(), osvAdvisory);
    }
}
