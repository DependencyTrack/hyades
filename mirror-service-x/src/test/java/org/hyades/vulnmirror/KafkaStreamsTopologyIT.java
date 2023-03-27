package org.hyades.vulnmirror;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.http.HttpHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.hyades.common.KafkaTopic;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.util.WireMockTestResource;
import org.hyades.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.http.Body.fromJsonBytes;
import static org.apache.commons.io.IOUtils.resourceToByteArray;
import static org.assertj.core.api.Assertions.assertThat;

@Suite
@SelectClasses(value = {
        KafkaStreamsTopologyIT.NvdMirrorIT.class,
        KafkaStreamsTopologyIT.GitHubMirrorIT.class
})
class KafkaStreamsTopologyIT {

    @QuarkusIntegrationTest
    @QuarkusTestResource(KafkaCompanionResource.class)
    @QuarkusTestResource(value = WireMockTestResource.class, initArgs = {
            @ResourceArg(name = "serverUrlProperty", value = "mirror.datasource.nvd.base-url")
    })
    static class NvdMirrorIT {

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() throws Exception {
            wireMock.stubFor(get(urlPathEqualTo("/"))
                    .withQueryParam("startIndex", equalTo("0"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/nvd/cves-page-01.json")))));

            wireMock.stubFor(get(urlPathEqualTo("/"))
                    .withQueryParam("startIndex", equalTo("2000"))
                    .willReturn(aResponse()
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withResponseBody(fromJsonBytes(resourceToByteArray("/datasource/nvd/cves-page-02.json")))));

            kafkaCompanion
                    .produce(Serdes.String(), Serdes.String())
                    .fromRecords(new ProducerRecord<>(KafkaTopic.VULNERABILITY_MIRROR_COMMAND.getName(), "NVD", null));

            final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                    .consume(Serdes.String(), new KafkaProtobufSerde<>(Bom.parser()))
                    .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 3, Duration.ofSeconds(15))
                    .awaitCompletion()
                    .getRecords();

            assertThat(results).satisfiesExactlyInAnyOrder(
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-40489");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-40489");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-40849");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-40849");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    },
                    record -> {
                        assertThat(record.key()).isEqualTo("NVD/CVE-2022-44262");
                        assertThat(record.value().getVulnerabilitiesCount()).isEqualTo(1);

                        final Vulnerability vuln = record.value().getVulnerabilities(0);
                        assertThat(vuln.getId()).isEqualTo("CVE-2022-44262");
                        assertThat(vuln.hasSource()).isTrue();
                        assertThat(vuln.getSource().getName()).isEqualTo("NVD");
                    }
            );
        }

    }

    @QuarkusIntegrationTest
    @QuarkusTestResource(KafkaCompanionResource.class)
    @QuarkusTestResource(value = WireMockTestResource.class, initArgs = {
            @ResourceArg(name = "serverUrlProperty", value = "mirror.datasource.github.base-url")
    })
    public static class GitHubMirrorIT {

        @InjectKafkaCompanion
        KafkaCompanion kafkaCompanion;

        @InjectWireMock
        WireMockServer wireMock;

        @Test
        void test() {

        }

    }

}