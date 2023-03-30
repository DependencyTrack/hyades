package org.hyades.vulnmirror.datasource.osv;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.hyades.common.KafkaTopic;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.notification.v1.Notification;
import org.hyades.vulnmirror.TestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.hyades.proto.notification.v1.Group.GROUP_DATASOURCE_MIRRORING;
import static org.hyades.proto.notification.v1.Level.LEVEL_ERROR;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.hyades.proto.notification.v1.Scope.SCOPE_SYSTEM;
import static org.hyades.vulnmirror.datasource.util.FileUtil.deleteFileAndDir;
import static org.hyades.vulnmirror.datasource.util.FileUtil.getTempFileLocation;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class OsvMirrorTest {

    @Inject
    OsvMirror osvMirror;

    @InjectMock
    OsvClient osvClientMock;

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    private Path tempZipLocation;

    @AfterEach
    void afterEach() {
        if (tempZipLocation != null) {
            deleteFileAndDir(tempZipLocation);
        }
    }

    @Test
    void testDoMirrorSuccessNotification() throws IOException {
        Path testFile = Path.of("src/test/resources/osv/osv-download.zip");
        tempZipLocation = getTempFileLocation("test", ".zip");
        Files.copy(testFile, tempZipLocation, StandardCopyOption.REPLACE_EXISTING);
        doReturn(tempZipLocation).when(osvClientMock).downloadEcosystemZip(anyString());
        assertThatNoException().isThrownBy(() -> osvMirror.doMirror("Maven").get());
        final List<ConsumerRecord<String, Notification>> notificationRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(notificationRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isNull();
                    assertThat(record.value().getScope()).isEqualTo(SCOPE_SYSTEM);
                    assertThat(record.value().getGroup()).isEqualTo(GROUP_DATASOURCE_MIRRORING);
                    assertThat(record.value().getLevel()).isEqualTo(LEVEL_INFORMATIONAL);
                    assertThat(record.value().getTitle()).isEqualTo("OSV Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("OSV mirroring completed for ecosystem: Maven");
                }
        );
    }

    @Test
    void testDoMirrorFailureNotification() throws IOException {
        doThrow(new IOException()).when(osvClientMock).downloadEcosystemZip(anyString());
        assertThatNoException().isThrownBy(() -> osvMirror.doMirror("Maven").get());

        final List<ConsumerRecord<String, Notification>> notificationRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(notificationRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isNull();
                    assertThat(record.value().getScope()).isEqualTo(SCOPE_SYSTEM);
                    assertThat(record.value().getGroup()).isEqualTo(GROUP_DATASOURCE_MIRRORING);
                    assertThat(record.value().getLevel()).isEqualTo(LEVEL_ERROR);
                    assertThat(record.value().getTitle()).isEqualTo("OSV Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("An error occurred mirroring the contents of ecosystem :Maven for OSV. Check log for details.");
                }
        );
    }

    @Test
    void testDoMirrorFailureNotificationWhenNoEcoSystemPassed() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> osvMirror.performMirror(null));
        assertThatNoException().isThrownBy(() -> osvMirror.doMirror(null).get());

        final List<ConsumerRecord<String, Notification>> notificationRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Notification.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NOTIFICATION_DATASOURCE_MIRRORING.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(notificationRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isNull();
                    assertThat(record.value().getScope()).isEqualTo(SCOPE_SYSTEM);
                    assertThat(record.value().getGroup()).isEqualTo(GROUP_DATASOURCE_MIRRORING);
                    assertThat(record.value().getLevel()).isEqualTo(LEVEL_ERROR);
                    assertThat(record.value().getTitle()).isEqualTo("OSV Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("An error occurred mirroring the contents of ecosystem :null for OSV. Check log for details.");
                }
        );
    }

    @Test
    void testPerformMirrorGo() throws Exception {
        Path testFile = Path.of("src/test/resources/osv/osv-download.zip");
        tempZipLocation = getTempFileLocation("test", ".zip");
        Files.copy(testFile, tempZipLocation, StandardCopyOption.REPLACE_EXISTING);
        doReturn(tempZipLocation).when(osvClientMock).downloadEcosystemZip(anyString());

        assertThatNoException().isThrownBy(() -> osvMirror.performMirror("Go"));

        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(vulnRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("OSV/GO-2020-0023");
                    assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                    final Vulnerability vuln = record.value().getVulnerabilities(0);
                    assertThat(vuln.getId()).isEqualTo("GO-2020-0023");
                    assertThat(vuln.hasSource()).isTrue();
                    assertThat(vuln.getSource().getName()).isEqualTo("OSV");
                }
        );
    }

    @Test
    void testPerformMirrorMaven() throws Exception {
        Path testFile = Path.of("src/test/resources/osv/maven.zip");
        tempZipLocation = getTempFileLocation("test", ".zip");
        Files.copy(testFile, tempZipLocation, StandardCopyOption.REPLACE_EXISTING);
        doReturn(tempZipLocation).when(osvClientMock).downloadEcosystemZip(anyString());

        assertThatNoException().isThrownBy(() -> osvMirror.performMirror("Maven"));

        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(vulnRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("OSV/GHSA-fxqr-px2m-fvc2");
                    assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                    final Vulnerability vuln = record.value().getVulnerabilities(0);
                    assertThat(vuln.getId()).isEqualTo("GHSA-fxqr-px2m-fvc2");
                    assertThat(vuln.hasSource()).isTrue();
                    assertThat(vuln.getSource().getName()).isEqualTo("GITHUB");
                }
        );
    }


}
