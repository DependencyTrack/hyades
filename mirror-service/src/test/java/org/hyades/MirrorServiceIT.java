package org.hyades;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.http.HttpStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.cyclonedx.model.Bom;
import org.hyades.common.KafkaTopic;
import org.hyades.util.WireMockTestResource;
import org.hyades.util.WireMockTestResource.InjectWireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusIntegrationTest
@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTestResource(
        value = WireMockTestResource.class,
        initArgs = @ResourceArg(name = "serverUrlProperty", value = "mirror.osv.base.url")
)
public class MirrorServiceIT {

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    @InjectWireMock
    WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() throws IOException {
        wireMockServer.stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withBody(getOsvTestZipData("osv-download.zip"))
                        .withStatus(HttpStatus.SC_OK)));
    }

    @Test
    void test() {
        kafkaCompanion
                .produce(Serdes.String(), Serdes.String())
                .fromRecords(new ProducerRecord<>(KafkaTopic.MIRROR_OSV.getName(), "Go", ""));

        final List<ConsumerRecord<String, Bom>> results = kafkaCompanion
                .consume(Serdes.String(), new ObjectMapperSerde<>(Bom.class))
                .fromTopics(KafkaTopic.NEW_VULNERABILITY.getName(), 1, Duration.ofSeconds(5))
                .awaitCompletion()
                .getRecords();

        assertThat(results).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo("OSV/GO-2020-0023");
                    assertThat(record.value()).isNotNull();

                    final Bom bom = record.value();
                    assertThat(bom.getComponents()).hasSize(1);
                    assertThat(bom.getVulnerabilities()).hasSize(1);
                }
        );
    }

    private byte[] getOsvTestZipData(final String name) throws IOException {
        final URL resourceUrl = getClass().getClassLoader().getResource(Paths.get("osv", name).toString());
        assertThat(resourceUrl).isNotNull();

        try (final InputStream inputStream = resourceUrl.openStream()) {
            return inputStream.readAllBytes();
        }
    }

}
