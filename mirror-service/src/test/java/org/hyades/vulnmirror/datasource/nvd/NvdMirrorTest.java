package org.hyades.vulnmirror.datasource.nvd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParseException;
import io.github.jeremylong.nvdlib.NvdApiException;
import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
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
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.apache.commons.io.IOUtils.resourceToByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.hyades.proto.notification.v1.Group.GROUP_DATASOURCE_MIRRORING;
import static org.hyades.proto.notification.v1.Level.LEVEL_ERROR;
import static org.hyades.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.hyades.proto.notification.v1.Scope.SCOPE_SYSTEM;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class NvdMirrorTest {

    @Inject
    NvdMirror nvdMirror;

    @InjectMock
    NvdApiClientFactory apiClientFactoryMock;

    @Inject
    ObjectMapper objectMapper;

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    @Test
    void testDoMirrorSuccessNotification() {
        final var apiClientMock = mock(NvdCveApi.class);
        when(apiClientMock.hasNext())
                .thenReturn(false);

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());

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
                    assertThat(record.value().getTitle()).isEqualTo("NVD Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("Mirroring of the National Vulnerability Database completed successfully.");
                }
        );
    }

    @Test
    void testDoMirrorFailureNotification() {
        final var apiClientMock = mock(NvdCveApi.class);
        when(apiClientMock.hasNext())
                .thenReturn(true);
        when(apiClientMock.next())
                .thenThrow(new IllegalStateException());

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());

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
                    assertThat(record.value().getTitle()).isEqualTo("NVD Mirroring");
                    assertThat(record.value().getContent()).isEqualTo("An error occurred mirroring the contents of the National Vulnerability Database, cause being: java.lang.IllegalStateException. Check log for details.");
                }
        );
    }

    @Test
    @SuppressWarnings("resource")
    void testMirrorInternal() throws Exception {
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        final var apiClientMock = mock(NvdCveApi.class);
        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenReturn(List.of(item));
        when(apiClientMock.getLastModifiedRequest())
                .thenReturn(1679922240L);

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.mirrorInternal());
        verify(apiClientFactoryMock).createApiClient(eq(0L));

        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(vulnRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
                    assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                    final Vulnerability vuln = record.value().getVulnerabilities(0);
                    assertThat(vuln.getId()).isEqualTo("CVE-2022-40489");
                    assertThat(vuln.hasSource()).isTrue();
                    assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                }
        );

        // Trigger a mirror operation one more time.
        // Verify that this time the previously stored "last updated" timestamp is used.
        assertThatNoException().isThrownBy(() -> nvdMirror.mirrorInternal());
        verify(apiClientFactoryMock).createApiClient(eq(1679922240L));
    }

    @Test
    void testRetryInCaseOfTwoConsecutiveExceptions() throws IOException {
        final var apiClientMock = mock(NvdCveApi.class);
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenReturn(List.of(item));
        when(apiClientMock.getLastModifiedRequest())
                .thenReturn(1679922240L);

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);

        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());
        final List<ConsumerRecord<String, Bom>> vulnRecords = kafkaCompanion
                .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                .withGroupId(TestConstants.CONSUMER_GROUP_ID)
                .withAutoCommit()
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(vulnRecords).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
                    assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                    final Vulnerability vuln = record.value().getVulnerabilities(0);
                    assertThat(vuln.getId()).isEqualTo("CVE-2022-40489");
                    assertThat(vuln.hasSource()).isTrue();
                    assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                }
        );

        // Trigger a mirror operation one more time.
        // Verify that this time the previously stored "last updated" timestamp is used.
        assertThatNoException().isThrownBy(() -> nvdMirror.mirrorInternal());
    }

    @Test
    void testRetryWithDoMirrorInCaseOfThreeConsecutiveExceptions() throws IOException {
        final var apiClientMock = mock(NvdCveApi.class);
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenReturn(List.of(item));
        when(apiClientMock.getLastModifiedRequest())
                .thenReturn(1679922240L);

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);
        assertThatNoException().isThrownBy(() -> nvdMirror.doMirror(null).get());

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
                    assertThat(record.value().getTitle()).isEqualTo("NVD Mirroring");
                    assertThat(record.value().getContent()).contains("io.github.jeremylong.nvdlib.NvdApiException");
                }
        );

    }

    @Test
    void testRetryWithMirrorInternalInCaseOfThreeConsecutiveExceptions() throws IOException {
        final var apiClientMock = mock(NvdCveApi.class);
        final var item = objectMapper.readValue(resourceToByteArray("/datasource/nvd/cve.json"), DefCveItem.class);

        when(apiClientMock.hasNext())
                .thenReturn(true)
                .thenReturn(false);
        when(apiClientMock.next())
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenThrow(NvdApiException.class)
                .thenReturn(List.of(item));

        when(apiClientFactoryMock.createApiClient(anyLong()))
                .thenReturn(apiClientMock);
        assertThatExceptionOfType(Exception.class).isThrownBy(() -> nvdMirror.mirrorInternal());

    }
}