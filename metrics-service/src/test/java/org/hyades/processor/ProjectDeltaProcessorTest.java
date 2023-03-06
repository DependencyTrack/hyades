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
import org.hyades.metrics.model.ProjectMetrics;
import org.hyades.metrics.model.Status;
import org.hyades.metrics.processor.ProjectProcessorSupplier;
import org.hyades.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyades.metrics.model.Status.DELETED;

@QuarkusTest
class ProjectDeltaProcessorTest {
    @Inject
    ProjectProcessorSupplier projectProcessorSupplier;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, ProjectMetrics> inputTopic;
    private TestOutputTopic<String, ProjectMetrics> outputTopic;

    private KeyValueStore<String, ProjectMetrics> store;

    private static final UUID PROJECT_UUID = UUID.randomUUID();

    @BeforeEach
    void beforeEach() {
        final var topology = new Topology();
        topology.addSource("sourceProcessor", new StringDeserializer(),
                new ObjectMapperDeserializer<>(ProjectMetrics.class), "input-topic");
        topology.addProcessor("deltaProjectProcessor", projectProcessorSupplier, "sourceProcessor");
        topology.addSink("sinkProcessor", "output-topic",
                new StringSerializer(), new ObjectMapperSerializer<>(), "deltaProjectProcessor");

        testDriver = new TopologyTestDriver(topology);
        store = testDriver.getKeyValueStore("delta-project-store");
        inputTopic = testDriver.createInputTopic("input-topic",
                new StringSerializer(), new ObjectMapperSerializer<>());
        outputTopic = testDriver.createOutputTopic("output-topic",
                new StringDeserializer(), new ObjectMapperDeserializer<>(ProjectMetrics.class));
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void shouldReturnProjectMetricsAsDeltaIfMetricsIsNotInStore() {

        final TestRecord<String, ProjectMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.getQueueSize()).isEqualTo(1);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getProject().getUuid().toString()).hasToString(PROJECT_UUID.toString());
            assertThat(record.getValue().getStatus()).isEqualTo(Status.CREATED);
            assertThat(record.getValue().getComponents()).isEqualTo(2);
            assertThat(record.getValue().getCritical()).isEqualTo(2);
            assertThat(record.getValue().getHigh()).isEqualTo(3);
            assertThat(record.getValue().getMedium()).isEqualTo(4);
            assertThat(record.getValue().getLow()).isEqualTo(5);
            assertThat(record.getValue().getVulnerabilities()).isEqualTo(2);
            assertThat(record.getValue().getVulnerableComponents()).isEqualTo(2);
            assertThat(record.getValue().getFindingsAudited()).isEqualTo(5);
            assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getFindingsTotal()).isEqualTo(10);
            assertThat(record.getValue().getPolicyViolationsFail()).isEqualTo(1);
            assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsInfo()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(10);
            assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(10);
            assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(5);
            assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(10);
        });
    }

    @Test
    void shouldReturnDeltaWithOnlyChangesWhenEventIsOfSameProject() {

        final TestRecord<String, ProjectMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);
        final TestRecord<String, ProjectMetrics> inputRecord2 = createTestRecord(1, 2, 4, 2);
        inputTopic.pipeInput(inputRecord2);

        assertThat(outputTopic.getQueueSize()).isEqualTo(2);

        assertThat(outputTopic.readRecordsToList()).satisfiesExactlyInAnyOrder(
                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(2);
                    assertThat(record.getValue().getProject().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.CREATED);
                    assertThat(record.getValue().getComponents()).isEqualTo(2);
                    assertThat(record.getValue().getCritical()).isEqualTo(2);
                    assertThat(record.getValue().getHigh()).isEqualTo(3);
                    assertThat(record.getValue().getMedium()).isEqualTo(4);
                    assertThat(record.getValue().getLow()).isEqualTo(5);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(2);
                    assertThat(record.getValue().getVulnerableComponents()).isEqualTo(2);
                    assertThat(record.getValue().getFindingsAudited()).isEqualTo(5);
                    assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getFindingsTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsFail()).isEqualTo(1);
                    assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsInfo()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(10);
                },

                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(-1);
                    assertThat(record.getValue().getProject().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.UPDATED);
                    assertThat(record.getValue().getHigh()).isEqualTo(-1);
                    assertThat(record.getValue().getMedium()).isZero();
                    assertThat(record.getValue().getLow()).isZero();
                    assertThat(record.getValue().getVulnerabilities()).isZero();
                    assertThat(record.getValue().getFindingsAudited()).isZero();
                    assertThat(record.getValue().getFindingsUnaudited()).isZero();
                    assertThat(record.getValue().getFindingsTotal()).isZero();
                    assertThat(record.getValue().getPolicyViolationsFail()).isZero();
                    assertThat(record.getValue().getPolicyViolationsInfo()).isZero();
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsFail()).isZero();
                    assertThat(record.getValue().getPolicyViolationsWarn()).isZero();
                    assertThat(record.getValue().getPolicyViolationsInfo()).isZero();
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isZero();
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isZero();
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isZero();
                }
        );
    }

    @Test
    void shouldRemoveProjectMetricsWhenTombstoneEventIsReceived() {

        final TestRecord<String, ProjectMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);
        inputTopic.pipeInput(new TestRecord<>(PROJECT_UUID.toString(), null));

        assertThat(outputTopic.readRecordsToList()).satisfiesExactlyInAnyOrder(
                record -> {
                    assertThat(record.getValue().getCritical()).isEqualTo(2);
                    assertThat(record.getValue().getProject().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(Status.CREATED);
                    assertThat(record.getValue().getComponents()).isEqualTo(2);
                    assertThat(record.getValue().getCritical()).isEqualTo(2);
                    assertThat(record.getValue().getHigh()).isEqualTo(3);
                    assertThat(record.getValue().getMedium()).isEqualTo(4);
                    assertThat(record.getValue().getLow()).isEqualTo(5);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(2);
                    assertThat(record.getValue().getVulnerableComponents()).isEqualTo(2);
                    assertThat(record.getValue().getFindingsAudited()).isEqualTo(5);
                    assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getFindingsTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsFail()).isEqualTo(1);
                    assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsInfo()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(10);
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(5);
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(10);
                },

                record -> {
                    assertThat(record.getValue().getProject().getUuid().toString()).hasToString(inputRecord.getKey());
                    assertThat(record.getValue().getStatus()).isEqualTo(DELETED);
                    assertThat(record.getValue().getComponents()).isEqualTo(-2);
                    assertThat(record.getValue().getCritical()).isEqualTo(-2);
                    assertThat(record.getValue().getHigh()).isEqualTo(-3);
                    assertThat(record.getValue().getMedium()).isEqualTo(-4);
                    assertThat(record.getValue().getLow()).isEqualTo(-5);
                    assertThat(record.getValue().getVulnerabilities()).isEqualTo(-2);
                    assertThat(record.getValue().getVulnerableComponents()).isEqualTo(-2);
                    assertThat(record.getValue().getFindingsAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getFindingsUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getFindingsTotal()).isEqualTo(-10);
                    assertThat(record.getValue().getPolicyViolationsFail()).isEqualTo(-1);
                    assertThat(record.getValue().getPolicyViolationsWarn()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsInfo()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
                    assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isEqualTo(-10);
                    assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isEqualTo(-10);
                    assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isEqualTo(-5);
                    assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isEqualTo(-10);

                }
        );
    }

    @Test
    void shouldSendNoChangeStatusIfNoChangeFromProjectMetricsInStore() {

        store.put(PROJECT_UUID.toString(), createProjectMetrics(2, 3, 4, 2));

        final TestRecord<String, ProjectMetrics> inputRecord = createTestRecord(2, 3, 4, 2);
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getStatus()).isEqualTo(Status.NO_CHANGE);
            assertThat(record.getValue().getComponents()).isZero();
            assertThat(record.getValue().getVulnerableComponents()).isZero();
            assertThat(record.getValue().getCritical()).isZero();
            assertThat(record.getValue().getHigh()).isZero();
            assertThat(record.getValue().getMedium()).isZero();
            assertThat(record.getValue().getLow()).isZero();
            assertThat(record.getValue().getVulnerabilities()).isZero();
            assertThat(record.getValue().getVulnerableComponents()).isZero();
            assertThat(record.getValue().getFindingsAudited()).isZero();
            assertThat(record.getValue().getFindingsUnaudited()).isZero();
            assertThat(record.getValue().getFindingsTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsFail()).isZero();
            assertThat(record.getValue().getPolicyViolationsWarn()).isZero();
            assertThat(record.getValue().getPolicyViolationsInfo()).isZero();
            assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isZero();
        });
    }

    @Test
    void shouldSendDeltaOfMetricsFromProjectMetricsInStore() {

        store.put(PROJECT_UUID.toString(), createProjectMetrics(2, 3, 4, 2));

        final TestRecord<String, ProjectMetrics> inputRecord = createRecordWithComponentDeleted();
        inputTopic.pipeInput(inputRecord);

        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo(inputRecord.getKey());
            assertThat(record.getValue().getStatus()).isEqualTo(Status.UPDATED);
            assertThat(record.getValue().getVulnerableComponents()).isZero();
            assertThat(record.getValue().getCritical()).isEqualTo(-2);
            assertThat(record.getValue().getHigh()).isEqualTo(-3);
            assertThat(record.getValue().getMedium()).isEqualTo(-4);
            assertThat(record.getValue().getLow()).isZero();
            assertThat(record.getValue().getVulnerabilities()).isEqualTo(-2);
            assertThat(record.getValue().getVulnerableComponents()).isZero();
            assertThat(record.getValue().getFindingsAudited()).isZero();
            assertThat(record.getValue().getFindingsUnaudited()).isZero();
            assertThat(record.getValue().getFindingsTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsFail()).isZero();
            assertThat(record.getValue().getPolicyViolationsWarn()).isZero();
            assertThat(record.getValue().getPolicyViolationsInfo()).isZero();
            assertThat(record.getValue().getPolicyViolationsAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsSecurityTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalTotal()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsOperationalAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseAudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseUnaudited()).isZero();
            assertThat(record.getValue().getPolicyViolationsLicenseTotal()).isZero();
        });
    }

    private TestRecord<String, ProjectMetrics> createTestRecord(int critical, int high, int medium, int vulnerabilities) {
        final var projectMetrics = createProjectMetrics(critical, high, medium, vulnerabilities);
        return new TestRecord<>(PROJECT_UUID.toString(), projectMetrics);
    }

    private TestRecord<String, ProjectMetrics> createRecordWithComponentDeleted() {
        var project = new Project();
        project.setUuid(PROJECT_UUID);
        var projectMetrics = new ProjectMetrics();
        projectMetrics.setProject(project);
        projectMetrics.setComponents(1);
        projectMetrics.setCritical(0);
        projectMetrics.setHigh(0);
        projectMetrics.setMedium(0);
        projectMetrics.setLow(5);
        projectMetrics.setVulnerabilities(0);
        projectMetrics.setVulnerableComponents(2);
        projectMetrics.setFindingsAudited(5);
        projectMetrics.setFindingsUnaudited(5);
        projectMetrics.setFindingsTotal(10);
        projectMetrics.setPolicyViolationsFail(1);
        projectMetrics.setPolicyViolationsWarn(5);
        projectMetrics.setPolicyViolationsInfo(5);
        projectMetrics.setPolicyViolationsAudited(0);
        projectMetrics.setPolicyViolationsUnaudited(0);
        projectMetrics.setFindingsUnaudited(5);
        projectMetrics.setPolicyViolationsOperationalAudited(5);
        projectMetrics.setPolicyViolationsOperationalUnaudited(5);
        projectMetrics.setPolicyViolationsOperationalTotal(10);
        projectMetrics.setPolicyViolationsSecurityAudited(5);
        projectMetrics.setPolicyViolationsSecurityUnaudited(5);
        projectMetrics.setPolicyViolationsSecurityTotal(10);
        projectMetrics.setPolicyViolationsLicenseAudited(5);
        projectMetrics.setPolicyViolationsLicenseAudited(5);
        projectMetrics.setPolicyViolationsLicenseUnaudited(5);
        projectMetrics.setPolicyViolationsLicenseTotal(10);
        return new TestRecord<>(PROJECT_UUID.toString(), projectMetrics);
    }

    private static ProjectMetrics createProjectMetrics(int critical, int high, int medium, int vulnerabilities) {
        var project = new Project();
        project.setUuid(PROJECT_UUID);
        var projectMetrics = new ProjectMetrics();
        projectMetrics.setProject(project);
        projectMetrics.setComponents(2);
        projectMetrics.setCritical(critical);
        projectMetrics.setHigh(high);
        projectMetrics.setMedium(medium);
        projectMetrics.setLow(5);
        projectMetrics.setVulnerabilities(vulnerabilities);
        projectMetrics.setVulnerableComponents(2);
        projectMetrics.setFindingsAudited(5);
        projectMetrics.setFindingsUnaudited(5);
        projectMetrics.setFindingsTotal(10);
        projectMetrics.setPolicyViolationsFail(1);
        projectMetrics.setPolicyViolationsWarn(5);
        projectMetrics.setPolicyViolationsInfo(5);
        projectMetrics.setPolicyViolationsAudited(0);
        projectMetrics.setPolicyViolationsUnaudited(0);
        projectMetrics.setFindingsUnaudited(5);
        projectMetrics.setPolicyViolationsOperationalAudited(5);
        projectMetrics.setPolicyViolationsOperationalUnaudited(5);
        projectMetrics.setPolicyViolationsOperationalTotal(10);
        projectMetrics.setPolicyViolationsSecurityAudited(5);
        projectMetrics.setPolicyViolationsSecurityUnaudited(5);
        projectMetrics.setPolicyViolationsSecurityTotal(10);
        projectMetrics.setPolicyViolationsLicenseAudited(5);
        projectMetrics.setPolicyViolationsLicenseAudited(5);
        projectMetrics.setPolicyViolationsLicenseUnaudited(5);
        projectMetrics.setPolicyViolationsLicenseTotal(10);
        return projectMetrics;
    }
}
