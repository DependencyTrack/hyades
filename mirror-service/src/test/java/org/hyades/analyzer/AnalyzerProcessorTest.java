package org.hyades.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.cyclonedx.model.Bom;
import org.hyades.model.AnalyzerIdentity;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;
import org.hyades.model.VulnerabilityScanStatus;
import org.hyades.model.VulnerableSoftware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class AnalyzerProcessorTest {

    @Inject
    AnalyzerProcessorSupplier processorSupplier;
    private TopologyTestDriver testDriver;
    private TestInputTopic<VulnerabilityScanKey, VulnerabilityScanResult> inputTopic;
    private TestOutputTopic<String, Bom> outputTopic;

    @BeforeEach
    void beforeEach() {
        final var topology = new Topology();
        topology.addSource("sourceProcessor", new ObjectMapperDeserializer<>(VulnerabilityScanKey.class),
                new ObjectMapperDeserializer<>(VulnerabilityScanResult.class), "input-topic");
        topology.addProcessor("analyzerProcessor", processorSupplier, "sourceProcessor");
        topology.addSink("sinkProcessor", "output-topic",
                new StringSerializer(), new ObjectMapperSerializer<>(), "analyzerProcessor");
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic("input-topic",
                new ObjectMapperSerializer<>(), new ObjectMapperSerializer<>());
        outputTopic = testDriver.createOutputTopic("output-topic",
                new StringDeserializer(), new ObjectMapperDeserializer<>(Bom.class));
    }

    @Test
    void testNoResults() {
        final var scanKey = new VulnerabilityScanKey(UUID.randomUUID().toString(), UUID.randomUUID());
        final var testRecord = new TestRecord<>(scanKey, new VulnerabilityScanResult(scanKey, AnalyzerIdentity.INTERNAL_ANALYZER,
                VulnerabilityScanStatus.COMPLETE, Collections.emptyList(), "na"));
        inputTopic.pipeInput(testRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(0);
    }

    @Test
    void testSingleResult() throws Exception {
        final TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> inputRecord = createTestRecord();
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        final TestRecord<String, Bom> outputRecord = outputTopic.readRecord();
        assertThat(outputRecord.key()).isEqualTo("OSSINDEX_ANALYZER/test-id");
        var outputVuln = outputRecord.getValue().getVulnerabilities().get(0);
        assertThat(outputVuln.getId()).isEqualTo("test-id");
        assertThat(outputVuln.getSource().getName()).isEqualTo("NVD");
        assertThat(outputVuln.getCredits().getIndividuals().get(0).getName()).isEqualTo("local");
        assertThat(outputVuln.getCwes().size()).isEqualTo(2);
        assertThat(outputVuln.getReferences().size()).isEqualTo(3);
        assertThat(outputVuln.getRatings().get(0).getSeverity())
                .isEqualTo(org.cyclonedx.model.vulnerability.Vulnerability.Rating.Severity.MEDIUM);
        assertThat(outputVuln.getAffects().size()).isEqualTo(1);
        var affectedPackage = outputVuln.getAffects().get(0);
        assertThat(affectedPackage.getVersions().get(0).getRange()).isEqualTo("vers:golang/>=1.1|<2.2");
        var components = outputRecord.getValue().getComponents();
        assertThat(components.size()).isEqualTo(1);
        assertThat(affectedPackage.getRef()).isEqualTo(components.get(0).getBomRef());
    }

    @Test
    void testMultipleResults() {
        final TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> inputRecord = createTestRecordWithMultipleVuln();
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(2);
        final List<TestRecord<String, Bom>> outputRecords = outputTopic.readRecordsToList();
        assertThat(outputRecords.get(0).getKey()).isEqualTo("INTERNAL_ANALYZER/analyzer-vuln-id-1");
        assertThat(outputRecords.get(1).getKey()).isEqualTo("INTERNAL_ANALYZER/analyzer-vuln-id-2");
    }

    private TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> createTestRecord() throws IOException {
        String jsonFile = "src/test/resources/analyzer/dt-vulnerability.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        org.hyades.model.Vulnerability vulnInput = new ObjectMapper().readValue(jsonString, org.hyades.model.Vulnerability.class);
        var vs = new VulnerableSoftware();
        vs.setVulnerable(true);
        vs.setVersionEndExcluding("2.2");
        vs.setVersionStartIncluding("1.1");
        vs.setPurl("pkg:golang/test");
        vulnInput.setVulnerableSoftware(List.of(vs));
        final var scanKey = new VulnerabilityScanKey(UUID.randomUUID().toString(), UUID.randomUUID());
        return new TestRecord<>(scanKey, new VulnerabilityScanResult(scanKey, AnalyzerIdentity.OSSINDEX_ANALYZER,
                VulnerabilityScanStatus.COMPLETE, List.of(vulnInput), "na"));
    }

    private TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> createTestRecordWithMultipleVuln() {
        var analyzerVuln1 = new org.hyades.model.Vulnerability();
        analyzerVuln1.setVulnId("analyzer-vuln-id-1");
        var analyzerVuln2 = new org.hyades.model.Vulnerability();
        analyzerVuln2.setVulnId("analyzer-vuln-id-2");
        final var scanKey = new VulnerabilityScanKey(UUID.randomUUID().toString(), UUID.randomUUID());
        return new TestRecord<>(scanKey, new VulnerabilityScanResult(scanKey, AnalyzerIdentity.INTERNAL_ANALYZER,
                VulnerabilityScanStatus.COMPLETE, List.of(analyzerVuln1, analyzerVuln2), "na"));
    }
}