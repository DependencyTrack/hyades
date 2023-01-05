package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.common.KafkaTopic;
import org.acme.model.OsvAdvisory;
import org.acme.osv.OsvAnalyzer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
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
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, OsvAdvisory> outputTopic;
    private OsvAnalyzer osvAnalyzerMock;

    @BeforeEach
    void beforeEach() {
        osvAnalyzerMock = Mockito.mock(OsvAnalyzer.class);
        QuarkusMock.installMockForType(osvAnalyzerMock, OsvAnalyzer.class);
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.MIRROR_OSV.getName(), new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(), new StringDeserializer(), new ObjectMapperDeserializer<>(OsvAdvisory.class));
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void testNoAdvisories() throws IOException {
        Mockito.when(osvAnalyzerMock.isEnabled()).thenReturn(true);
        Mockito.when(osvAnalyzerMock.performMirror(Mockito.anyString())).thenReturn(Collections.emptyList());
        inputTopic.pipeInput("Maven", "");
        Assertions.assertTrue(outputTopic.isEmpty());
    }

    @Test
    void testOsvMirroring() throws IOException {
        OsvAdvisory osvAdvisory = new OsvAdvisory();
        osvAdvisory.setId("test-id");
        Mockito.when(osvAnalyzerMock.isEnabled()).thenReturn(true);
        Mockito.when(osvAnalyzerMock.performMirror(Mockito.anyString())).thenReturn(List.of(osvAdvisory));
        inputTopic.pipeInput("Go", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo("OSV/test-id");
        });
    }
}
