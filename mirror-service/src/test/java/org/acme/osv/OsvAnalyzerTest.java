package org.acme.osv;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.acme.client.OsvClient;
import org.acme.common.KafkaTopic;
import org.acme.model.OsvAdvisory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.acme.util.FileUtil.deleteFileAndDir;
import static org.acme.util.FileUtil.getTempFileLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class OsvAnalyzerTest {

    @Inject
    Topology topology;

    @InjectMock
    OsvClient osvClientMock;

    @Inject
    OsvAnalyzer osvAnalyzer;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, OsvAdvisory> outputTopic;

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.MIRROR_OSV.getName(),
                new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.NEW_VULNERABILITY.getName(),
                new StringDeserializer(), new ObjectMapperDeserializer<>(OsvAdvisory.class));
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
    }

    @Test
    void performMirrorFromTopic() throws IOException {

        Path testFile = Path.of("src/test/resources/osv/osv-download.zip");
        Path tempZipLocation = getTempFileLocation("test", ".zip");
        Files.copy(testFile, tempZipLocation, StandardCopyOption.REPLACE_EXISTING);
        when(osvClientMock.downloadEcosystemZip(anyString()))
                .thenReturn(tempZipLocation);
        inputTopic.pipeInput("Go", "");
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecord()).satisfies(record -> {
            assertThat(record.key()).isEqualTo("OSV/GO-2020-0023");
        });
        deleteFileAndDir(tempZipLocation);
    }

    @Test
    void performMirrorReturnOsvAdvisories() throws IOException {

        Path testFile = Path.of("src/test/resources/osv/osv-download.zip");
        Path tempZipLocation = getTempFileLocation("test", ".zip");
        Files.copy(testFile, tempZipLocation, StandardCopyOption.REPLACE_EXISTING);
        when(osvClientMock.downloadEcosystemZip(anyString()))
                .thenReturn(tempZipLocation);
        List<OsvAdvisory> result = osvAnalyzer.performMirror("Go");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo("GO-2020-0023");
        deleteFileAndDir(tempZipLocation);
    }
}
