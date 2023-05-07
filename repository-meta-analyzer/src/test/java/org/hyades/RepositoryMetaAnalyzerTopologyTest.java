package org.hyades;

import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.hyades.common.KafkaTopic;
import org.hyades.model.MetaAnalyzerCacheKey;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.KafkaProtobufDeserializer;
import org.hyades.proto.KafkaProtobufSerializer;
import org.hyades.proto.repometaanalysis.v1.AnalysisCommand;
import org.hyades.proto.repometaanalysis.v1.AnalysisResult;
import org.hyades.repositories.IMetaAnalyzer;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class RepositoryMetaAnalyzerTopologyTest {

    @Inject
    Topology topology;

    @InjectMock
    RepositoryAnalyzerFactory analyzerFactoryMock;

    @InjectMock
    RepoEntityRepository repoEntityRepositoryMock;

    @Inject
    @CacheName("metaAnalyzer")
    Cache cache;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, AnalysisCommand> inputTopic;
    private TestOutputTopic<String, AnalysisResult> outputTopic;
    private IMetaAnalyzer analyzerMock;

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(
                KafkaTopic.REPO_META_ANALYSIS_COMMAND.getName(),
                new StringSerializer(), new KafkaProtobufSerializer<>());
        outputTopic = testDriver.createOutputTopic(
                KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(),
                new StringDeserializer(), new KafkaProtobufDeserializer<>(AnalysisResult.parser()));

        analyzerMock = mock(IMetaAnalyzer.class);

        when(analyzerFactoryMock.hasApplicableAnalyzer(any(PackageURL.class)))
                .thenReturn(true);
        when(analyzerFactoryMock.createAnalyzer(any(PackageURL.class)))
                .thenReturn(Optional.of(analyzerMock));
    }

    @Test
    void testAnalyzerCacheMiss() throws Exception {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2")
                        .build())
                .build();

        // mock repository data
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setUrl("https://repo1.maven.org/maven2/");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        // mock maven analyzer analyze method
        final MetaModel outputMetaModel = new MetaModel();
        outputMetaModel.setLatestVersion("test");
        when(analyzerMock.analyze(any())).thenReturn(outputMetaModel);
        when(analyzerMock.getName()).thenReturn("testAnalyzer");

        inputTopic.pipeInput("foo", command);

        final var cacheKey = new MetaAnalyzerCacheKey("testAnalyzer",
                "pkg:maven/com.fasterxml.jackson.core/jackson-databind",
                "https://repo1.maven.org/maven2/");
        Assertions.assertNotNull(cache.as(CaffeineCache.class).getIfPresent(cacheKey).get());
    }

    @Test
    void testAnalyzerCacheHit() {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2"))
                .build();

        // mock repository data
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setUrl("https://repo1.maven.org/maven2/");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        // mock maven analyzer analyze method
        final var result = AnalysisResult.newBuilder()
                .setComponent(command.getComponent())
                .setRepository("testRepository")
                .setLatestVersion("test")
                .build();
        when(analyzerMock.getName()).thenReturn("testAnalyzer");

        // populate the cache to hit the match
        final var cacheKey = new MetaAnalyzerCacheKey("testAnalyzer",
                "pkg:maven/com.fasterxml.jackson.core/jackson-databind",
                "https://repo1.maven.org/maven2/");
        cache.as(CaffeineCache.class).put(cacheKey, completedFuture(result));

        inputTopic.pipeInput("foo", command);
        final KeyValue<String, AnalysisResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind", record.key);
        Assertions.assertEquals("test", record.value.getLatestVersion());
    }


    @Test
    void testPerformMetaAnalysis() {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2"))
                .build();

        inputTopic.pipeInput("foo", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
    }


    @Test
    void testNoPurlComponent() {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder())
                .build();

        inputTopic.pipeInput("foo", command);

        Assertions.assertEquals(0, outputTopic.getQueueSize());
    }

    @Test
    void testMetaOutput() {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2"))
                .build();

        inputTopic.pipeInput("foo", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<String, AnalysisResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind", record.key);
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2", record.value.getComponent().getPurl());
        Assertions.assertFalse(record.value.hasLatestVersion());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        cache.invalidateAll();
    }
}

