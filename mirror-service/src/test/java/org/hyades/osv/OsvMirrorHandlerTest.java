package org.hyades.osv;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.cyclonedx.model.Bom;
import org.hyades.common.KafkaTopic;
import org.hyades.osv.client.OsvClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyades.util.FileUtil.deleteFileAndDir;
import static org.hyades.util.FileUtil.getTempFileLocation;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class OsvMirrorHandlerTest {

    @Inject
    Topology topology;

    @InjectMock
    OsvClient osvClientMock;

    @Inject
    OsvMirrorHandler osvMirrorHandler;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Bom> outputTopic;
    private Path tempZipLocation;

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.MIRROR_OSV.getName(),
                new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(),
                new StringDeserializer(), new ObjectMapperDeserializer<>(Bom.class));
    }

    @AfterEach
    void afterEach() {
        deleteFileAndDir(tempZipLocation);
        testDriver.close();
    }

    @Test
    void performMirrorFromTopic() throws IOException {

        Path testFile = Path.of("src/test/resources/osv/osv-download.zip");
        tempZipLocation = getTempFileLocation("test", ".zip");
        Files.copy(testFile, tempZipLocation, StandardCopyOption.REPLACE_EXISTING);
        when(osvClientMock.downloadEcosystemZip(anyString()))
                .thenReturn(tempZipLocation);
        inputTopic.pipeInput("Go", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo("OSV/GO-2020-0023");
        });
    }
}
