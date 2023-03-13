package org.hyades.metrics.processor;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.TestRecord;
import org.hyades.metrics.model.ComponentMetrics;
import org.hyades.metrics.model.Status;
import org.hyades.metrics.model.VulnerabilityStatus;
import org.hyades.model.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ComponentDeltaProcessorTest {

    @Inject
    ComponentProcessorSupplier componentProcessorSupplier;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, ComponentMetrics> inputTopic;
    private TestOutputTopic<String, ComponentMetrics> outputTopic;

    private KeyValueStore<String, ComponentMetrics> store;

    private static final String TEST_PURL_JACKSON_BIND = "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4";
    private static final UUID COMPONENT_UUID = UUID.randomUUID();

    @BeforeEach
    void beforeEach() {
        final var topology = new Topology();
        topology.addSource("sourceProcessor", new StringDeserializer(),
                new ObjectMapperDeserializer<>(ComponentMetrics.class), "input-topic");
        topology.addProcessor("deltaProcessor", componentProcessorSupplier, "sourceProcessor");
        topology.addSink("sinkProcessor", "output-topic",
                new StringSerializer(), new ObjectMapperSerializer<>(), "deltaProcessor");

        testDriver = new TopologyTestDriver(topology);
        store = testDriver.getKeyValueStore("delta-component-store");
        inputTopic = testDriver.createInputTopic("input-topic",
                new StringSerializer(), new ObjectMapperSerializer<>());
        outputTopic = testDriver.createOutputTopic("output-topic",
                new StringDeserializer(), new ObjectMapperDeserializer<>(ComponentMetrics.class));
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void shouldReturnComponentMetricsAsDeltaIfComponentWasNotInStore() {

        final TestRecord<String, ComponentMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.getQueueSize()).isEqualTo(1);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getStatus()).isEqualTo(Status.CREATED);
            assertThat(record.getValue().getCritical()).isEqualTo(2);
            assertThat(record.getValue().getHigh()).isEqualTo(3);
            assertThat(record.getValue().getMedium()).isEqualTo(4);
            assertThat(record.getValue().getLow()).isEqualTo(5);
            assertThat(record.getValue().getVulnerabilities()).isEqualTo(2);
            assertThat(record.getValue().getFindingsAudited()).isEqualTo(1);
            assertThat(record.getValue().getFindingsTotal()).isEqualTo(10);
            assertThat(record.getValue().getPolicyViolationsFail()).isEqualTo(1);
            assertThat(record.getValue().getPolicyViolationsInfo()).isEqualTo(2);
            assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
            assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getSuppressed()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(1);
            assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(10);
            assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(10);
            assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(10);
        });
    }

    @Test
    void shouldReturnDeltaWithOnlyChangesWhenEventIsOfSameComponent() {

        final TestRecord<String, ComponentMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);
        final TestRecord<String, ComponentMetrics> inputRecord2 = createTestRecord(1, 2, 4, 2);
        inputTopic.pipeInput(inputRecord2);

        assertThat(outputTopic.getQueueSize()).isEqualTo(2);

        assertThat(outputTopic.readRecordsToList()).satisfiesExactlyInAnyOrder(
                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(2);
                    assertThat(record.getValue().getComponent().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.CREATED);
                    assertThat(record.getValue().getHigh()).isEqualTo(3);
                    assertThat(record.getValue().getMedium()).isEqualTo(4);
                    assertThat(record.getValue().getLow()).isEqualTo(5);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(2);
                    assertThat(record.getValue().getFindingsAudited()).isEqualTo(1);
                    assertThat(record.getValue().getFindingsTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsFail()).isEqualTo(1);
                    assertThat(record.getValue().getPolicyViolationsInfo()).isEqualTo(2);
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getSuppressed()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(1);
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(10);
                },

                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(-1);
                    assertThat(record.getValue().getComponent().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.UPDATED);
                    assertThat(record.getValue().getHigh()).isEqualTo(-1);
                    assertThat(record.getValue().getMedium()).isZero();
                    assertThat(record.getValue().getLow()).isZero();
                    assertThat(record.getValue().getVulnerabilities()).isZero();
                    assertThat(record.getValue().getFindingsAudited()).isZero();
                    assertThat(record.getValue().getFindingsTotal()).isZero();
                    assertThat(record.getValue().getPolicyViolationsFail()).isZero();
                    assertThat(record.getValue().getPolicyViolationsInfo()).isZero();
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getFindingsUnaudited()).isZero();
                    assertThat(record.getValue().getSuppressed()).isZero();
                    assertThat(record.getValue().getPolicyViolationsWarn()).isZero();
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isZero();
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isZero();
                }
        );
    }

    @Test
    void shouldRemoveComponentMetricsWhenTombstoneEventIsReceived() {

        final TestRecord<String, ComponentMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);
        inputTopic.pipeInput(new TestRecord<>(COMPONENT_UUID.toString(), null));

        assertThat(outputTopic.readRecordsToList()).satisfiesExactlyInAnyOrder(
                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(2);
                    assertThat(record.getValue().getComponent().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.CREATED);
                    assertThat(record.getValue().getHigh()).isEqualTo(3);
                    assertThat(record.getValue().getMedium()).isEqualTo(4);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(2);
                    assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getSuppressed()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(1);
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(10);
                },

                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(-2);
                    assertThat(record.getValue().getComponent().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.DELETED);
                    assertThat(record.getValue().getHigh()).isEqualTo(-3);
                    assertThat(record.getValue().getMedium()).isEqualTo(-4);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(-2);
                    assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getSuppressed()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(-1);
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(-10);
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(-10);
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(-10);
                }
        );
    }

    @Test
    void shouldSendNoChangeStatusIfNoChangeFromComponentMetricsInStore() {

        store.put(COMPONENT_UUID.toString(), createComponentMetrics(2, 3, 4, 2));

        final TestRecord<String, ComponentMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getStatus()).isEqualTo(Status.NO_CHANGE);
            assertThat(record.getValue().getCritical()).isZero();
            assertThat(record.getValue().getHigh()).isZero();
            assertThat(record.getValue().getMedium()).isZero();
            assertThat(record.getValue().getLow()).isZero();
            assertThat(record.getValue().getVulnerabilities()).isZero();
            assertThat(record.getValue().getFindingsAudited()).isZero();
            assertThat(record.getValue().getFindingsTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsFail()).isZero();
            assertThat(record.getValue().getPolicyViolationsInfo()).isZero();
            assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
            assertThat(record.getValue().getFindingsUnaudited()).isZero();
            assertThat(record.getValue().getSuppressed()).isZero();
            assertThat(record.getValue().getPolicyViolationsWarn()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isZero();
        });
    }

    @Test
    void shouldSetVulnerabilityStatusToNoChangeIfVulnerableComponentRemainsSo() {
        store.put(COMPONENT_UUID.toString(), createComponentMetrics(2, 3, 4, 9));

        final TestRecord<String, ComponentMetrics> inputRecord = createTestRecord(0, 3, 4, 7);
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getVulnerabilityStatus()).isEqualTo(VulnerabilityStatus.NO_CHANGE);
        });

    }

    @Test
    void shouldSetVulnerabilityStatusToNotVulnerableIfVulnerabilitiesFixed() {
        store.put(COMPONENT_UUID.toString(), createComponentMetrics(2, 3, 4, 9));

        final TestRecord<String, ComponentMetrics> inputRecord = createTestRecord(0, 0, 0, 0);
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getVulnerabilityStatus()).isEqualTo(VulnerabilityStatus.NOT_VULNERABLE);
        });

    }

    private TestRecord<String, ComponentMetrics> createTestRecord(int critical, int high, int medium, int vulnerabilities) {
        final var componentMetrics = createComponentMetrics(critical, high, medium, vulnerabilities);
        return new TestRecord<>(COMPONENT_UUID.toString(), componentMetrics);
    }

    private static ComponentMetrics createComponentMetrics(int critical, int high, int medium, int vulnerabilities) {
        final var component = new Component();
        component.setUuid(COMPONENT_UUID);
        component.setPurl(TEST_PURL_JACKSON_BIND);
        ComponentMetrics componentMetrics = new ComponentMetrics();
        componentMetrics.setCritical(critical);
        componentMetrics.setHigh(high);
        componentMetrics.setMedium(medium);
        componentMetrics.setVulnerabilities(vulnerabilities);
        componentMetrics.setComponent(component);
        componentMetrics.setFindingsAudited(1);
        componentMetrics.setLow(5);
        componentMetrics.setFindingsTotal(10);
        componentMetrics.setPolicyViolationsFail(1);
        componentMetrics.setPolicyViolationsInfo(2);
        componentMetrics.setPolicyViolationsAudited(0);
        componentMetrics.setPolicyViolationsUnaudited(0);
        componentMetrics.setFindingsUnaudited(5);
        componentMetrics.setSuppressed(5);
        componentMetrics.setPolicyViolationsWarn(1);
        componentMetrics.setPolicyViolationsLicenseAudited(5);
        componentMetrics.setPolicyViolationsLicenseUnaudited(5);
        componentMetrics.setPolicyViolationsLicenseTotal(10);
        componentMetrics.setPolicyViolationsOperationalAudited(5);
        componentMetrics.setPolicyViolationsOperationalUnaudited(5);
        componentMetrics.setPolicyViolationsOperationalTotal(10);
        componentMetrics.setPolicyViolationsSecurityAudited(5);
        componentMetrics.setPolicyViolationsSecurityUnaudited(5);
        componentMetrics.setPolicyViolationsSecurityTotal(10);
        return componentMetrics;
    }
}
