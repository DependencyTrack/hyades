package org.hyades;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.hyades.common.KafkaTopic;
import org.hyades.model.IntegrityAnalysisCacheKey;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.KafkaProtobufDeserializer;
import org.hyades.proto.KafkaProtobufSerializer;
import org.hyades.proto.repometaanalysis.v1.AnalysisCommand;
import org.hyades.proto.repometaanalysis.v1.HashMatchStatus;
import org.hyades.proto.repometaanalysis.v1.IntegrityResult;
import org.hyades.repositories.IntegrityAnalyzer;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.concurrent.CompletableFuture.completedFuture;
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
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Header[] headers = new Header[]{
                new BasicHeader("X-CheckSum-MD5", "098f6bcd4621d373cade4e832627b4f6"),
                new BasicHeader("X-Checksum-SHA1", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"),
                new BasicHeader("X-Checksum-SHA256", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        };
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);

        when(analyzerMock.getIntegrityCheckResponse(any())).thenReturn(response);
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString())
                        .build())
                .build();

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
    void testAnalyzerCacheHit() {
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
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Header[] headers = new Header[]{
                new BasicHeader("X-CheckSum-MD5", "098f6bcd4621d373cade4e832627b4f6"),
                new BasicHeader("X-Checksum-SHA1", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"),
                new BasicHeader("X-Checksum-SHA256", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        };
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);

        when(repoEntityRepositoryMock.findEnabledRepositoriesByType(any()))
                .thenReturn(List.of(repository));

        // mock maven analyzer analyze method

        when(analyzerMock.getName()).thenReturn("testAnalyzer");

        // populate the cache to hit the match
        final var cacheKey = new IntegrityAnalysisCacheKey("testRepository", "https://localhost",
                "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2"
        );
        cache.as(CaffeineCache.class).put(cacheKey, completedFuture(response));

        inputTopic.pipeInput("pkg:maven/com.fasterxml.jackson.core/jackson-databind", command);
        final KeyValue<String, IntegrityResult> record = outputTopic.readKeyValue();
        Assertions.assertEquals("pkg:maven/com.fasterxml.jackson.core/jackson-databind", record.key);
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_PASS, record.value.getSha256HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_PASS, record.value.getSha1HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_PASS, record.value.getMd5HashMatch());
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
    void testMetaOutputPass() throws IOException, MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-core@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Header[] headers = new Header[]{
                new BasicHeader("X-CheckSum-MD5", "098f6bcd4621d373cade4e832627b4f6"),
                new BasicHeader("X-Checksum-SHA1", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"),
                new BasicHeader("X-Checksum-SHA256", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        };
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);

        when(analyzerMock.getIntegrityCheckResponse(any())).thenReturn(response);
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
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_PASS, record.value.getMd5HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_PASS, record.value.getSha1HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_PASS, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputComponentMissingHash() throws IOException, MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind1@2.13.2")
                        .setMd5Hash("")
                        .setSha1Hash("")
                        .setSha256Hash("")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Header[] headers = new Header[]{
                new BasicHeader("X-CheckSum-MD5", "098f6bcd4621d373cade4e832627b4f6"),
                new BasicHeader("X-Checksum-SHA1", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"),
                new BasicHeader("X-Checksum-SHA256", "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        };
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);

        when(analyzerMock.getIntegrityCheckResponse(any())).thenReturn(response);
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
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH, record.value.getMd5HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH, record.value.getSha1HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_COMPONENT_MISSING_HASH, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputSourceMissingHash() throws IOException, MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind2@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f5")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Header[] headers = new Header[]{};
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);

        when(analyzerMock.getIntegrityCheckResponse(any())).thenReturn(response);
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
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN, record.value.getMd5HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN, record.value.getSha1HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_UNKNOWN, record.value.getSha256HashMatch());
    }

    @Test
    void testMetaOutputFail() throws IOException, MalformedPackageURLException {
        final var command = AnalysisCommand.newBuilder()
                .setComponent(org.hyades.proto.repometaanalysis.v1.Component.newBuilder()
                        .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind3@2.13.2")
                        .setMd5Hash("098f6bcd4621d373cade4e832627b4f6")
                        .setSha1Hash("a94a8fe5ccb19ba61c4c0873d391e987982fvbd3")
                        .setSha256Hash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
                        .setUuid(UUID.randomUUID().toString()))
                .build();
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        Header[] headers = new Header[]{
                new BasicHeader("X-CheckSum-MD5", "098f6bcd4621d373cade4e832627b4f5"),
                new BasicHeader("X-Checksum-SHA1", "a94a8fe5ccb19ba61c4c0873d341e987982fbbd3"),
                new BasicHeader("X-Checksum-SHA256", "9f86d081884c7d659a2feaa0f55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        };
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);

        when(analyzerMock.getIntegrityCheckResponse(any())).thenReturn(response);
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
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_FAIL, record.value.getMd5HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_FAIL, record.value.getSha1HashMatch());
        Assertions.assertEquals(HashMatchStatus.HASH_MATCH_STATUS_FAIL, record.value.getSha256HashMatch());
    }
}