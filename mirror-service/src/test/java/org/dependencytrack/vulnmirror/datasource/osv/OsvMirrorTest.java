package org.dependencytrack.vulnmirror.datasource.osv;

import com.google.protobuf.util.JsonFormat;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import jakarta.inject.Inject;
import net.javacrumbs.jsonunit.core.Option;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_4.Bom;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.proto.KafkaProtobufSerde;
import org.dependencytrack.vulnmirror.TestConstants;
import org.hamcrest.Matchers;
import org.dependencytrack.proto.notification.v1.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.dependencytrack.proto.notification.v1.Group.GROUP_DATASOURCE_MIRRORING;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.proto.notification.v1.Scope.SCOPE_SYSTEM;
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
    void afterEach() throws Exception {
        // Publish tombstones to the vulnerability digest topic for all vulnerabilities used in this test.
        kafkaCompanion.produce(Serdes.String(), Serdes.ByteArray())
                .fromRecords(List.of(
                        new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "OSV/GO-2020-0023", null),
                        new ProducerRecord<>(KafkaTopic.VULNERABILITY_DIGEST.getName(), "OSV/GHSA-2cc5-23r7-vc4v", null)
                ))
                .awaitCompletion();

        if (tempZipLocation != null && Files.exists(tempZipLocation)) {
            Files.delete(tempZipLocation);
        }
    }

    @Test
    void testDoMirrorSuccessNotification() throws IOException {
        Path testFile = Path.of("src/test/resources/datasource/osv/osv-download.zip");
        tempZipLocation = Files.createTempFile("test", ".zip");
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
                    assertThat(record.value().getContent()).isEqualTo("Tried to mirror null ecosystem for OSV.");
                }
        );
    }

    @Test
    void testPerformMirrorGo() throws Exception {
        Path testFile = Path.of("src/test/resources/datasource/osv/osv-download.zip");
        tempZipLocation = Files.createTempFile("test", ".zip");
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

        assertThat(vulnRecords).satisfiesExactly(record -> {
            assertThat(record.key()).isEqualTo("OSV/GO-2020-0023");
            assertThat(record.value()).isNotNull();
        });

        assertThatJson(JsonFormat.printer().print(vulnRecords.get(0).value()))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .withMatcher("vuln-description", Matchers.allOf(
                        Matchers.startsWith("Token validation methods are susceptible to"),
                        Matchers.hasLength(216)))
                .isEqualTo("""
                        {
                          "components": [
                            {
                              "bomRef": "2aa501b7-09d2-5bb7-88cc-41d599869255",
                              "name": "github.com/robbert229/jwt",
                              "purl": "pkg:golang/github.com/robbert229/jwt"
                            }
                          ],
                          "vulnerabilities": [
                            {
                              "id": "GO-2020-0023",
                              "source": { "name": "OSV" },
                              "description": "${json-unit.matches:vuln-description}",
                              "published": "2022-06-09T07:01:32Z",
                              "updated": "2022-06-09T07:01:32Z",
                              "ratings": [
                                { "severity": "SEVERITY_UNKNOWN" }
                              ],
                              "affects": [
                                {
                                  "ref": "2aa501b7-09d2-5bb7-88cc-41d599869255",
                                  "versions": [
                                    { "range": "vers:golang/<0.0.0-20170426191122-ca1404ee6e83" }
                                  ]
                                }
                              ]
                            }
                          ],
                          "externalReferences": [
                            { "url": "https://github.com/robbert229/jwt/commit/ca1404ee6e83fcbafb66b09ed0d543850a15b654" },
                            { "url": "https://github.com/robbert229/jwt/issues/12" }
                          ]
                        }
                        """);
    }

    @Test
    void testPerformMirrorMaven() throws Exception {
        Path testFile = Path.of("src/test/resources/datasource/osv/maven.zip");
        tempZipLocation = Files.createTempFile("test", ".zip");
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

        assertThat(vulnRecords).satisfiesExactly(record -> {
            assertThat(record.key()).isEqualTo("OSV/GHSA-2cc5-23r7-vc4v");
            assertThat(record.value()).isNotNull();
        });

        assertThatJson(JsonFormat.printer().print(vulnRecords.get(0).value()))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .withMatcher("vuln-description", Matchers.allOf(
                        Matchers.startsWith("### Impact"),
                        Matchers.hasLength(952)))
                .isEqualTo("""
                        {
                          "components": [
                            {
                              "bomRef": "2a24a29f-9ff3-52b8-bc81-471f326a5b3e",
                              "name": "io.ratpack:ratpack-session",
                              "purl": "pkg:maven/io.ratpack/ratpack-session"
                            }
                          ],
                          "vulnerabilities": [
                            {
                              "id": "GHSA-2cc5-23r7-vc4v",
                              "source": { "name": "GITHUB" },
                              "description": "${json-unit.matches:vuln-description}",
                              "cwes": [ 330, 340 ],
                              "published": "2021-07-01T17:02:26Z",
                              "updated": "2023-03-28T05:45:27Z",
                              "ratings": [
                                {
                                  "method": "SCORE_METHOD_CVSSV3",
                                  "score": 4.4,
                                  "severity": "SEVERITY_MEDIUM",
                                  "vector": "CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:L/I:L/A:N"
                                }
                              ],
                              "advisories": [
                                { "url": "https://nvd.nist.gov/vuln/detail/CVE-2021-29480" }
                              ],
                              "properties": [
                                  {
                                    "name": "dependency-track:vuln:title",
                                    "value": "Ratpack's default client side session signing key is highly predictable"
                                  }
                              ],
                              "affects": [
                                {
                                  "ref": "2a24a29f-9ff3-52b8-bc81-471f326a5b3e",
                                  "versions": [
                                    { "range": "vers:maven/<1.9.0" }
                                  ]
                                }
                              ]
                            }
                          ],
                          "externalReferences": [
                            { "url": "https://github.com/ratpack/ratpack/security/advisories/GHSA-2cc5-23r7-vc4v" },
                            { "url": "https://github.com/ratpack/ratpack" },
                            { "url": "https://github.com/ratpack/ratpack/blob/29434f7ac6fd4b36a4495429b70f4c8163100332/ratpack-session/src/main/java/ratpack/session/clientside/ClientSideSessionConfig.java#L29" }
                          ]
                        }
                        """);
    }


}
