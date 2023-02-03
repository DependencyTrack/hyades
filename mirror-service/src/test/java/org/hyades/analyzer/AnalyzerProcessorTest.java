package org.hyades.analyzer;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.cyclonedx.model.Bom;
import org.hyades.model.AnalyzerIdentity;
import org.hyades.model.Component;
import org.hyades.model.VulnerabilityScanKey;
import org.hyades.model.VulnerabilityScanResult;
import org.hyades.model.VulnerabilityScanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
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
        topology.addSource("sourceProcessor", new StringDeserializer(),
                new ObjectMapperDeserializer<>(VulnerabilityScanResult.class), "input-topic");
        topology.addProcessor("analyzerProcessor", processorSupplier, "sourceProcessor");
        topology.addSink("sinkProcessor", "output-topic",
                new ObjectMapperSerializer<>(), new ObjectMapperSerializer<>(), "analyzerProcessor");
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
        assertThat(outputRecord.key()).isEqualTo("OSSINDEX_ANALYZER/analyzer-vuln-id");
        assertThat(outputRecord.getValue().getVulnerabilities().get(0).getId())
                .isEqualTo("analyzer-vuln-id");
        assertThat(outputRecord.getValue().getComponents().get(0).getPurl())
                .isEqualTo("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.1");
    }

    @Test
    void testMultipleResults() throws Exception {
        final TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> inputRecord = createTestRecordWithMultipleVuln();
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(2);
        final List<TestRecord<String, Bom>> outputRecords = outputTopic.readRecordsToList();
        assertThat(outputRecords.get(0).getKey()).isEqualTo("INTERNAL_ANALYZER/analyzer-vuln-id-1");
        assertThat(outputRecords.get(1).getKey()).isEqualTo("INTERNAL_ANALYZER/analyzer-vuln-id-2");
    }

    private TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> createTestRecord() throws MalformedPackageURLException {
        var analyzerVuln = new org.hyades.model.Vulnerability();
        analyzerVuln.setVulnId("analyzer-vuln-id");
        final var component = new Component();
        component.setUuid(UUID.randomUUID());
        final PackageURL packageURL = new PackageURL("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.1");
        component.setPurl(packageURL);
        analyzerVuln.setComponents(List.of(component));
        final var scanKey = new VulnerabilityScanKey(UUID.randomUUID().toString(), component.getUuid());
        return new TestRecord<>(scanKey, new VulnerabilityScanResult(scanKey, AnalyzerIdentity.OSSINDEX_ANALYZER,
                VulnerabilityScanStatus.COMPLETE, List.of(analyzerVuln), "na"));
    }

    private TestRecord<VulnerabilityScanKey, VulnerabilityScanResult> createTestRecordWithMultipleVuln() throws MalformedPackageURLException {
        var analyzerVuln1 = new org.hyades.model.Vulnerability();
        analyzerVuln1.setVulnId("analyzer-vuln-id-1");
        var analyzerVuln2 = new org.hyades.model.Vulnerability();
        analyzerVuln2.setVulnId("analyzer-vuln-id-2");
        final var scanKey = new VulnerabilityScanKey(UUID.randomUUID().toString(), UUID.randomUUID());
        return new TestRecord<>(scanKey, new VulnerabilityScanResult(scanKey, AnalyzerIdentity.INTERNAL_ANALYZER,
                VulnerabilityScanStatus.COMPLETE, List.of(analyzerVuln1, analyzerVuln2), "na"));
    }
}