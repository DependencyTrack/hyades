package org.hyades.processor;

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
import org.hyades.metrics.processor.ComponentProcessorSupplier;
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
                },

                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(-2);
                    assertThat(record.getValue().getComponent().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.DELETED);
                    assertThat(record.getValue().getHigh()).isEqualTo(-3);
                    assertThat(record.getValue().getMedium()).isEqualTo(-4);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(-2);
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
        return componentMetrics;
    }
}
