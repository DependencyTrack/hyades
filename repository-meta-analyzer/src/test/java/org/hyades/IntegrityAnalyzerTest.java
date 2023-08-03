package org.hyades;

import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.hyades.common.KafkaTopic;
import org.hyades.model.IntegrityAnalysisCacheKey;
import org.hyades.model.IntegrityModel;
import org.hyades.model.MetaAnalyzerCacheKey;
import org.hyades.model.MetaModel;
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.KafkaProtobufDeserializer;
import org.hyades.proto.KafkaProtobufSerializer;
import org.hyades.proto.repometaanalysis.v1.AnalysisCommand;
import org.hyades.proto.repometaanalysis.v1.AnalysisResult;
import org.hyades.proto.repometaanalysis.v1.HashMatchStatus;
import org.hyades.proto.repometaanalysis.v1.IntegrityResult;
import org.hyades.repositories.IMetaAnalyzer;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.hyades.serde.KafkaPurlSerde;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class IntegrityAnalyzerTest {

    @Inject
    Topology topology;

    @InjectMock
    RepositoryAnalyzerFactory analyzerFactoryMock;

    @InjectMock
    RepoEntityRepository repoEntityRepositoryMock;

    @Inject
    @CacheName("integrityAnalyzer")
    Cache integrityAnalyzerCache;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, AnalysisCommand> inputTopic;
    private TestOutputTopic<PackageURL, IntegrityResult> integrityResultTestOutputTopic;
    private IMetaAnalyzer analyzerMock;

    final KafkaPurlSerde purlSerde = new KafkaPurlSerde();

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(
                KafkaTopic.REPO_META_ANALYSIS_COMMAND.getName(),
                new StringSerializer(), new KafkaProtobufSerializer<>());
        integrityResultTestOutputTopic = testDriver.createOutputTopic(
                KafkaTopic.INTEGRITY_ANALYSIS_RESULT.getName(),
                purlSerde.deserializer(), new KafkaProtobufDeserializer<>(IntegrityResult.parser()));


        analyzerMock = mock(IMetaAnalyzer.class);

        when(analyzerFactoryMock.hasApplicableAnalyzer(any(PackageURL.class)))
                .thenReturn(true);
        when(analyzerFactoryMock.createAnalyzer(any(PackageURL.class)))
                .thenReturn(Optional.of(analyzerMock));
    }


    @Test
    void testIntegrityAnalyzerCacheMiss() throws Exception {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2")
                        .build())
                .build();

        // mock repository data
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setAuthenticationRequired(false);
        repository.setUrl("https://repo1.maven.org/maven2/");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        Component component = new Component();
        component.setUuid(UUID.fromString("test"));
        component.setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");

        // mock maven analyzer analyze method
        final IntegrityModel integrityModel = new IntegrityModel();
        integrityModel.setComponent(component);
        integrityModel.setSha256HashMatched(HashMatchStatus.PASS);
        integrityModel.setMd5HashMatched(HashMatchStatus.PASS);
        integrityModel.setSha1HashMatched(HashMatchStatus.PASS);
        integrityModel.setPublishedTimestamp(Timestamp.from(Instant.ofEpochSecond(System.currentTimeMillis())));
        when(analyzerMock.checkIntegrityOfComponent(any(), any())).thenReturn(integrityModel);
        when(analyzerMock.getName()).thenReturn("testAnalyzer");

        inputTopic.pipeInput("foo", command);

        final var cacheKey = new IntegrityAnalysisCacheKey("testAnalyzer",

                "https://repo1.maven.org/maven2/", "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");
        Assertions.assertNotNull(integrityAnalyzerCache.as(CaffeineCache.class).getIfPresent(cacheKey).get());
    }

      @AfterEach
    void afterEach() {
        testDriver.close();
        integrityAnalyzerCache.invalidateAll();
    }
}

