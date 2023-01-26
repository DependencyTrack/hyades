package org.hyades.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.cyclonedx.model.Bom;
import org.hyades.client.NvdClientConfig;
import org.hyades.common.KafkaTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@QuarkusTest
public class NvdMirrorHandlerTest {

    @Inject
    private Topology topology;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Bom> outputTopic;

    @Produces
    @Mock
    @ApplicationScoped
    @Named("NvdCveApiSupplier")
    public BiFunction<String, Long, NvdCveApi> cveApiSupplier() throws IOException {
        String jsonFile = "src/test/resources/nvd/nvd-response.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        DefCveItem defCveItem = new NvdClientConfig().objectMapper().readValue(jsonString, DefCveItem.class);

        final var cveApiMock = mock(NvdCveApi.class);
        Mockito.when(cveApiMock.hasNext()).thenReturn(true).thenReturn(false);
        Mockito.when(cveApiMock.next()).thenReturn(List.of(defCveItem));
        return (apiKey, lastModified) -> cveApiMock;
    }

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
        Mockito.reset();
    }

    @Test
    void performMirrorFromTopic() {
        inputTopic.pipeInput("event-test-id", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record ->
                assertThat(record.key()).isEqualTo("NVD/CVE-1999-1341"));
    }
}
