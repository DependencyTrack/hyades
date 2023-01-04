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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
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

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void testNoEcosystems() {
        Mockito.when(osvAnalyzerMock.isEnabled()).thenReturn(true);
        final UUID uuid = UUID.randomUUID();
        // empty string for enabled ecosystems
        inputTopic.pipeInput(uuid, "");
        Assertions.assertTrue(outputTopic.isEmpty());
    }

    @Test
    void testOsvMirroring() {
        OsvAdvisory osvAdvisory = new OsvAdvisory();
        osvAdvisory.setId("test-id");
        Mockito.when(osvAnalyzerMock.isEnabled()).thenReturn(true);
        Mockito.when(osvAnalyzerMock.performMirror(Mockito.anyString())).thenReturn(List.of(osvAdvisory));
        final UUID uuid = UUID.randomUUID();
        inputTopic.pipeInput(uuid, "test");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo("OSV/test-id");
        });
    }
}
