package org.hyades;

import com.github.packageurl.MalformedPackageURLException;
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
import org.hyades.persistence.model.Component;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.KafkaProtobufDeserializer;
import org.hyades.proto.KafkaProtobufSerializer;
import org.hyades.proto.repointegrityanalysis.v1.IntegrityResult;
import org.hyades.proto.repometaanalysis.v1.AnalysisCommand;
import org.hyades.repositories.IntegrityAnalyzer;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH;
import static org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus.HASH_MATCH_STATUS_FAIL;
import static org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus.HASH_MATCH_STATUS_PASS;
import static org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class RepositoryIntegrityAnalysisTopologyTest {
    @Inject
    Topology topology;

    @InjectMock
    RepositoryAnalyzerFactory analyzerFactoryMock;


    @InjectMock
    RepoEntityRepository repoEntityRepositoryMock;

    @Inject
    @CacheName("integrityAnalyzer")
    Cache cache;

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, AnalysisCommand> inputTopic;
    private TestOutputTopic<String, IntegrityResult> outputTopic;
    private IntegrityAnalyzer analyzerMock;


    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(
                KafkaTopic.INTEGRITY_ANALYSIS_COMMAND.getName(),
                new StringSerializer(), new KafkaProtobufSerializer<>());
        outputTopic = testDriver.createOutputTopic(
                KafkaTopic.INTEGRITY_ANALYSIS_RESULT.getName(),
                new StringDeserializer(), new KafkaProtobufDeserializer<>(IntegrityResult.parser()));

        analyzerMock = mock(IntegrityAnalyzer.class);

        when(analyzerFactoryMock.hasApplicableAnalyzer(any(PackageURL.class)))
                .thenReturn(true);
        when(analyzerFactoryMock.createIntegrityAnalyzer(any(PackageURL.class)))
                .thenReturn(Optional.of(analyzerMock));

        when(analyzerMock.getName()).thenReturn("testAnalyzer");
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        cache.invalidateAll();

    }

    @Test
    void testIntegrityAnalyzerCacheMiss() throws Exception {

        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString())
                        .build())
                .build();

        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_PASS);
        when(analyzerMock.getIntegrityModel(any())).thenReturn(integrityModelMock);

        // mock repository data
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setAuthenticationRequired(false);
        repository.setIntegrityCheckApplicable(true);
        repository.setUrl("http://localhost");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-databind", command);

        final var cacheKey = new IntegrityAnalysisCacheKey("testRepository",
                "http://localhost",
                "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");
        Assertions.assertNotNull(cache.as(CaffeineCache.class).getIfPresent(cacheKey).get());
    }

    @Test
    void testAnalyzerCacheHit() throws MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString()))
                .build();

        // mock repository data
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setIntegrityCheckApplicable(true);
        repository.setAuthenticationRequired(false);
        repository.setUrl("https://localhost");

        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_PASS);

        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        // mock maven analyzer analyze method

        when(analyzerMock.getName()).thenReturn("testAnalyzer");

        // populate the cache to hit the match
        final var cacheKey = new IntegrityAnalysisCacheKey("testRepository", "https://localhost",
                "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2"
        );
        cache.as(CaffeineCache.class).put(cacheKey, completedFuture(integrityModelMock));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-databind", command);
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind", record.key);
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getSha256HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getSha1HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getMd5HashMatch());
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
    void testMetaOutputPass() throws MalformedPackageURLException {
        var uuid = UUID.randomUUID();
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-core@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(uuid.toString()))
                .build();

        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_PASS);
        when(analyzerMock.getIntegrityModel(any())).thenReturn(integrityModelMock);

        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setAuthenticationRequired(false);
        repository.setIntegrityCheckApplicable(true);
        repository.setUrl("http://localhost");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-core", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-core", record.key);
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-core@2.13.2", record.value.getComponent().getPurl());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getMd5HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getSha1HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputComponentMissingHash() throws MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind1@2.13.2")
                        .setMd5Hash("")
                        .setSha1Hash("")
                        .setSha256Hash("")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_COMPONENT_MISSING_HASH);

        when(analyzerMock.getIntegrityModel(any())).thenReturn(integrityModelMock);
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setAuthenticationRequired(false);
        repository.setIntegrityCheckApplicable(true);
        repository.setUrl("http://localhost");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-databind1", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind1", record.key);
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind1@2.13.2", record.value.getComponent().getPurl());
        Assertions.assertEquals(HASH_MATCH_STATUS_COMPONENT_MISSING_HASH, record.value.getMd5HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_COMPONENT_MISSING_HASH, record.value.getSha1HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_COMPONENT_MISSING_HASH, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputSourceMissingHash() throws MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind2@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f5")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_UNKNOWN);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_UNKNOWN);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_UNKNOWN);

        when(analyzerMock.getIntegrityModel(any())).thenReturn(integrityModelMock);
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setAuthenticationRequired(false);
        repository.setIntegrityCheckApplicable(true);
        repository.setUrl("http://localhost");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-databind2", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind2", record.key);
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind2@2.13.2", record.value.getComponent().getPurl());
        Assertions.assertEquals(HASH_MATCH_STATUS_UNKNOWN, record.value.getMd5HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_UNKNOWN, record.value.getSha1HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_UNKNOWN, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputFail() throws MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind3@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fvbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_FAIL);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_FAIL);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_FAIL);

        when(analyzerMock.getIntegrityModel(any())).thenReturn(integrityModelMock);
        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(false);
        repository.setAuthenticationRequired(false);
        repository.setIntegrityCheckApplicable(true);
        repository.setUrl("http://localhost");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-databind3", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind3", record.key);
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind3@2.13.2", record.value.getComponent().getPurl());
        Assertions.assertEquals(HASH_MATCH_STATUS_FAIL, record.value.getMd5HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_FAIL, record.value.getSha1HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_FAIL, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputPassInternalRepo() throws MalformedPackageURLException {
        var uuid = UUID.randomUUID();
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:npm/@apollo/federation@0.19.1")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(uuid.toString()))
                .build();

        var integrityModelMock = createMockIntegrityModel(command);
        integrityModelMock.setHashMatchStatusMd5(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha1(HASH_MATCH_STATUS_PASS);
        integrityModelMock.setHashMatchStatusSha256(HASH_MATCH_STATUS_PASS);
        when(analyzerMock.getIntegrityModel(any())).thenReturn(integrityModelMock);

        Repository repository = new Repository();
        repository.setIdentifier("testRepository");
        repository.setInternal(true);
        repository.setAuthenticationRequired(true);
        repository.setUsername("username");
        repository.setPassword("password");
        repository.setIntegrityCheckApplicable(true);
        repository.setUrl("http://localhost");
        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        inputTopic.pipeInput("pkg:npm/@apollo/federation@0.19.1", command);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:npm/%40apollo/federation@0.19.1", record.key);
        Assertions.assertEquals("pkg:npm/%40apollo/federation@0.19.1", record.value.getComponent().getPurl());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getMd5HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getSha1HashMatch());
        Assertions.assertEquals(HASH_MATCH_STATUS_PASS, record.value.getSha256HashMatch());
    }

    private IntegrityModel createMockIntegrityModel(AnalysisCommand analysisCommand) throws MalformedPackageURLException {
        var component = new Component();
        var analysisComponent = analysisCommand.getComponent();
        component.setUuid(UUID.fromString(analysisComponent.getUuid()));
        component.setPurl(new PackageURL(analysisComponent.getPurl()));
        component.setMd5(analysisComponent.getMd5Hash());
        component.setSha1(analysisComponent.getSha1Hash());
        component.setSha256(analysisComponent.getSha256Hash());
        var integrityModelMock = new IntegrityModel();
        integrityModelMock.setComponent(component);
        return integrityModelMock;
    }
}