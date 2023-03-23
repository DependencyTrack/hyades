package org.hyades;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.hyades.common.KafkaTopic;
import org.hyades.nvd.NvdProcessorConfig;
import org.hyades.nvd.NvdProcessorSupplier;
import org.hyades.osv.OsvMirrorHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class MirrorServiceTopologyTest {

    @Inject
    Topology topology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopicOsv;
    private TestInputTopic<String, String> inputTopicNvd;
    private TestOutputTopic<String, Bom> outputTopic;
    private OsvMirrorHandler osvMirrorHandlerMock;

    @Produces
    @Mock
    @ApplicationScoped
    @Named("NvdCveApiSupplier")
    public BiFunction<String, Long, NvdCveApi> cveApiSupplier() throws IOException {
        String jsonFile = "src/test/resources/nvd/nvd-response.json";
        String jsonString = new String(Files.readAllBytes(Paths.get(jsonFile)));
        DefCveItem defCveItem = new NvdProcessorConfig().objectMapper().readValue(jsonString, DefCveItem.class);

        final var cveApiMock = mock(NvdCveApi.class);
        when(cveApiMock.hasNext()).thenReturn(true).thenReturn(false);
        when(cveApiMock.next()).thenReturn(List.of(defCveItem));
        return (apiKey, lastModified) -> cveApiMock;
    }

    @BeforeEach
    void beforeEach() {
        osvMirrorHandlerMock = Mockito.mock(OsvMirrorHandler.class);
        QuarkusMock.installMockForType(osvMirrorHandlerMock, OsvMirrorHandler.class);
        final var nvdProcessorSupplierMock = mock(NvdProcessorSupplier.class);
        when(nvdProcessorSupplierMock.get()).thenAnswer(invocation ->
                new MockScannerProcessor(event -> new Bom()));
        testDriver = new TopologyTestDriver(topology);
        inputTopicOsv = testDriver.createInputTopic(KafkaTopic.MIRROR_OSV.getName(),
                new StringSerializer(), new StringSerializer());
        inputTopicNvd = testDriver.createInputTopic(KafkaTopic.MIRROR_NVD.getName(),
                new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(),
                new StringDeserializer(), new ObjectMapperDeserializer<>(Bom.class));
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void testNoOsvAdvisories() throws IOException {
        when(osvMirrorHandlerMock.performMirror(Mockito.anyString())).thenReturn(Collections.emptyList());
        inputTopicOsv.pipeInput("Maven", "");
        Assertions.assertTrue(outputTopic.isEmpty());
    }

    @Test
    void testMirroring() throws IOException {
        Bom bov = new Bom();
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setId("test-id");
        bov.setVulnerabilities(List.of(vulnerability));
        when(osvMirrorHandlerMock.performMirror(Mockito.anyString())).thenReturn(List.of(bov));
        inputTopicNvd.pipeInput("event-test", "");
        inputTopicOsv.pipeInput("Go", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(2);
        assertThat(outputTopic.readRecordsToList()).satisfies(records -> {
            assertThat(records.get(0).getKey()).isEqualTo("NVD/CVE-1999-1341");
            assertThat(records.get(1).getKey()).isEqualTo("OSV/test-id");
        });
    }

    private static class MockScannerProcessor extends ContextualProcessor<String, String, String, Bom> {

        private final Function<String, Bom> processorFunction;

        private MockScannerProcessor(final Function<String, Bom> processorFunction) {
            this.processorFunction = processorFunction;
        }

        @Override
        public void process(final Record<String, String> record) {
            context().forward(record.withValue(processorFunction.apply(record.value())));
        }
    }
}
