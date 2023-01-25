package org.acme.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.client.NvdClient;
import org.acme.client.NvdClientConfig;
import org.acme.common.KafkaTopic;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class NvdMirrorHandlerTest {

    @Inject
    private Topology topology;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Bom> outputTopic;

    @Inject
    NvdClientConfig nvdClientConfig;

    @Inject
    NvdClient nvdClient;

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.MIRROR_NVD.getName(),
                new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(),
                new StringDeserializer(), new ObjectMapperDeserializer<>(Bom.class));
        nvdClientConfig = new NvdClientConfig();
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void performMirrorFromTopic() throws IOException {
        String jsonFile = "src/test/resources/nvd/nvd-response.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        DefCveItem defCveItem = nvdClientConfig.objectMapper().readValue(jsonString, DefCveItem.class);

        final var cveApiMock = Mockito.mock(NvdCveApi.class);
        Mockito.when(cveApiMock.hasNext()).thenReturn(true);
        Mockito.when(cveApiMock.next()).thenReturn(List.of(defCveItem));
        final BiFunction<String, Long, NvdCveApi> cveApiSupplier = (apiKey, lastModified) -> {
            return cveApiMock;
        };

        nvdClient = new NvdClient(nvdClientConfig.lastModifiedEpochStoreBuilder(), "test-apiKey", cveApiSupplier);
        QuarkusMock.installMockForType(nvdClient, NvdClient.class);
        inputTopic.pipeInput("event-test-id", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record ->
                assertThat(record.key()).isEqualTo("NVD/CVE-1999-1341"));
    }
}
