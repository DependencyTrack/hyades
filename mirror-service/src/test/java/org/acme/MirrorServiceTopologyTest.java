package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.common.KafkaTopic;
import org.acme.model.OsvAdvisory;
import org.acme.osv.OsvAnalyzer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.UUID;

@QuarkusTest
public class MirrorServiceTopologyTest {

    @Inject
    Topology topology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<UUID, String> inputTopic;
    private TestOutputTopic<String, OsvAdvisory> outputTopic;
    private OsvAnalyzer osvAnalyzerMock;

    @BeforeEach
    void beforeEach() {
        osvAnalyzerMock = Mockito.mock(OsvAnalyzer.class);
        QuarkusMock.installMockForType(osvAnalyzerMock, OsvAnalyzer.class);
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.MIRROR_OSV.getName(), new UUIDSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(), new StringDeserializer(), new ObjectMapperDeserializer<>(OsvAdvisory.class));
    }

    @Test
    void testNoEcosystems() {
        Mockito.when(osvAnalyzerMock.isEnabled()).thenReturn(true);
        final UUID uuid = UUID.randomUUID();
        final String ecosystems = "";
        inputTopic.pipeInput(uuid, ecosystems);
        Assertions.assertTrue(outputTopic.isEmpty());
    }
}
