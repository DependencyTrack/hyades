package org.acme.nvd;

import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
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

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
public class NvdMirrorHandlerTest {

    @Inject
    private Topology topology;

    @InjectMock
    private NvdClient nvdClient;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Bom> outputTopic;
    NvdClientConfig nvdClientConfig = new NvdClientConfig();

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.MIRROR_NVD.getName(),
                new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(),
                new StringDeserializer(), new ObjectMapperDeserializer<>(Bom.class));
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
        when(nvdClient.update())
                .thenReturn(List.of(defCveItem));
        inputTopic.pipeInput("event-test-id", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record ->
                assertThat(record.key()).isEqualTo("NVD/CVE-1999-1341"));
    }
}
