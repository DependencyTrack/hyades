package org.hyades;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.cyclonedx.model.Bom;
import org.hyades.analyzer.AnalyzerProcessor;
import org.hyades.common.KafkaTopic;
import org.hyades.model.Vulnerability;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;
import org.hyades.nvd.NvdProcessorSupplier;
import org.hyades.osv.OsvMirrorHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.hyades.commonutil.KafkaStreamsUtil.processorNameConsume;
import static org.hyades.commonutil.KafkaStreamsUtil.processorNameProduce;

@ApplicationScoped
public class MirrorServiceTopology {

    private final OsvMirrorHandler osvMirrorHandler;
    private final NvdProcessorSupplier nvdProcessorSupplier;

    @Inject
    public MirrorServiceTopology(OsvMirrorHandler osvMirrorHandler,
                                 NvdProcessorSupplier nvdProcessorSupplier) {
        this.osvMirrorHandler = osvMirrorHandler;
        this.nvdProcessorSupplier = nvdProcessorSupplier;
    }

    @Produces
    public Topology topology() {

        var streamsBuilder = new StreamsBuilder();
        var cyclonedxSerde = new ObjectMapperSerde<>(Bom.class);
        var scanKeySerde = new ObjectMapperSerde<>(VulnerabilityScanKey.class);
        var scanResultSerde = new ObjectMapperSerde<>(VulnerabilityScanResult.class);

        // OSV mirroring stream
        // (K,V) to be consumed as (String ecosystem, null)
        KStream<String, String> mirrorOsv = streamsBuilder
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
                        .with(Serdes.String(), cyclonedxSerde)
                        .withName(processorNameProduce(KafkaTopic.NEW_VULNERABILITY, "osv_vulnerability")));

        // NVD mirroring stream
        // (K,V) to be consumed as (event uuid, null)
        KStream<String, String> mirrorNvd = streamsBuilder
                .stream(KafkaTopic.MIRROR_NVD.getName(), Consumed
                        .with(Serdes.String(), Serdes.String())
                        .withName(processorNameConsume(KafkaTopic.MIRROR_NVD)));
        mirrorNvd
                .process(nvdProcessorSupplier, Named.as("mirror_nvd_vulnerabilities"))
                .to(KafkaTopic.NEW_VULNERABILITY.getName(), Produced
                        .with(Serdes.String(), cyclonedxSerde)
                        .withName(processorNameProduce(KafkaTopic.NEW_VULNERABILITY, "nvd_vulnerability")));

        // Vulnerability analyzers stream
        // (K,V) to be consumed as (event ScanKey, ScanResult) from analyzers result topic
        KStream<VulnerabilityScanKey, VulnerabilityScanResult> analyzerStream = streamsBuilder
                .stream(KafkaTopic.VULN_ANALYSIS_RESULT.getName(), Consumed
                        .with(scanKeySerde, scanResultSerde)
                        .withName(processorNameConsume(KafkaTopic.VULN_ANALYSIS_RESULT)));
        analyzerStream
                .process(AnalyzerProcessor::new, Named.as("analyzers_vulnerabilities"))
                .to(KafkaTopic.NEW_VULNERABILITY.getName(), Produced
                        .with(Serdes.String(), cyclonedxSerde)
                        .withName(processorNameProduce(KafkaTopic.NEW_VULNERABILITY, "analyzer_vulnerability")));

        return streamsBuilder.build();
    }

    List<KeyValue<String, Bom>> mirrorOsv(String ecosystem) throws IOException {
        return osvMirrorHandler.performMirror(ecosystem).stream()
                .map(bom -> KeyValue.pair(Vulnerability.Source.OSV.name() + "/" + bom.getVulnerabilities().get(0).getId(), bom))
                .toList();
    }
}
