package org.hyades;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.hyades.common.KafkaTopic;
import org.hyades.nvd.NvdMirrorHandler;
import org.hyades.osv.OsvMirrorHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class MirrorServiceTopologyTest {

    @Inject
    Topology topology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopicOsv;
    private TestInputTopic<String, String> inputTopicNvd;
    private TestOutputTopic<String, Bom> outputTopic;
    private OsvMirrorHandler osvMirrorHandlerMock;
    private NvdMirrorHandler nvdMirrorHandlerMock;

    @BeforeEach
    void beforeEach() {
        osvMirrorHandlerMock = Mockito.mock(OsvMirrorHandler.class);
        QuarkusMock.installMockForType(osvMirrorHandlerMock, OsvMirrorHandler.class);
        nvdMirrorHandlerMock = Mockito.mock(NvdMirrorHandler.class);
        QuarkusMock.installMockForType(nvdMirrorHandlerMock, NvdMirrorHandler.class);
        testDriver = new TopologyTestDriver(topology);
        inputTopicOsv = testDriver.createInputTopic(KafkaTopic.MIRROR_OSV.getName(), new StringSerializer(), new StringSerializer());
        inputTopicNvd = testDriver.createInputTopic(KafkaTopic.MIRROR_NVD.getName(), new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(), new StringDeserializer(), new ObjectMapperDeserializer<>(Bom.class));
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void testNoOsvAdvisories() throws IOException {
        Mockito.when(osvMirrorHandlerMock.performMirror(Mockito.anyString())).thenReturn(Collections.emptyList());
        inputTopicOsv.pipeInput("Maven", "");
        Assertions.assertTrue(outputTopic.isEmpty());
    }

//    @Test
//    void testNoNvdAdvisories() {
//        Mockito.when(nvdMirrorHandlerMock.performMirror()).thenReturn(Collections.emptyList());
//        inputTopicNvd.pipeInput("", "");
//        Assertions.assertTrue(outputTopic.isEmpty());
//    }

    @Test
    void testMirroring() throws IOException {
        Bom bov = new Bom();
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setId("test-id");
        bov.setVulnerabilities(List.of(vulnerability));
        Mockito.when(osvMirrorHandlerMock.performMirror(Mockito.anyString())).thenReturn(List.of(bov));
//        Mockito.when(nvdMirrorHandlerMock.performMirror()).thenReturn(List.of(bov));
        inputTopicOsv.pipeInput("Go", "");
        inputTopicNvd.pipeInput("", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(2);
        assertThat(outputTopic.readRecordsToList()).satisfies(records -> {
            assertThat(records.get(0).getKey()).isEqualTo("OSV/test-id");
            assertThat(records.get(1).getKey()).isEqualTo("NVD/test-id");
        });
    }
}
