package org.hyades.processor;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.MediaType;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.TestRecord;
import org.hyades.common.SecretDecryptor;
import org.hyades.persistence.model.RepositoryType;
import org.hyades.persistence.repository.RepoEntityRepository;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.repometaanalysis.v1.AnalysisCommand;
import org.hyades.proto.repometaanalysis.v1.AnalysisResult;
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.hyades.serde.KafkaPurlSerde;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@QuarkusTest
@TestProfile(MetaAnalyzerProcessorTest.TestProfile.class)
class MetaAnalyzerProcessorTest {

    public static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.kafka.snappy.enabled", "false"
            );
        }
    }

    private static ClientAndServer mockServer;

    private static final String TEST_PURL_JACKSON_BIND = "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4";

    private TopologyTestDriver testDriver;
    private TestInputTopic<PackageURL, AnalysisCommand> inputTopic;
    private TestOutputTopic<PackageURL, AnalysisResult> outputTopic;
    @Inject
    RepoEntityRepository repoEntityRepository;

    @Inject
    RepositoryAnalyzerFactory analyzerFactory;

    @Inject
    EntityManager entityManager;

    @Inject
    @CacheName("metaAnalyzer")
    Cache cache;

    @Inject
    SecretDecryptor secretDecryptor;

    @BeforeAll
    static void beforeClass() {
        mockServer = ClientAndServer.startClientAndServer(1080);
    }

    @BeforeEach
    void beforeEach() {
        final var processorSupplier = new MetaAnalyzerProcessorSupplier(repoEntityRepository, analyzerFactory, secretDecryptor, cache);

        final var valueSerde = new KafkaProtobufSerde<>(AnalysisCommand.parser());
        final var purlSerde = new KafkaPurlSerde();
        final var valueSerdeResult = new KafkaProtobufSerde<>(AnalysisResult.parser());

        final var streamsBuilder = new StreamsBuilder();
        streamsBuilder
                .stream("input-topic", Consumed.with(purlSerde, valueSerde))
                .processValues(processorSupplier)
                .to("output-topic", Produced.with(purlSerde, valueSerdeResult));

        testDriver = new TopologyTestDriver(streamsBuilder.build());
        inputTopic = testDriver.createInputTopic("input-topic", purlSerde.serializer(), valueSerde.serializer());
        outputTopic = testDriver.createOutputTopic("output-topic", purlSerde.deserializer(), valueSerdeResult.deserializer());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        cache.invalidateAll().await().indefinitely();
    }

    @AfterAll
    static void afterClass() {
        mockServer.stop();
    }

    @Test
    void testWithNoSupportedRepositoryTypes() throws Exception {
        final TestRecord<PackageURL, AnalysisCommand> inputRecord = new TestRecord<>(new PackageURL(TEST_PURL_JACKSON_BIND), AnalysisCommand.newBuilder().setComponent(Component.newBuilder()
                .setPurl(TEST_PURL_JACKSON_BIND)).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.MAVEN.toString().toLowerCase());
                });
    }

    @Test
    void testMalformedPurl() throws Exception {
        final TestRecord<PackageURL, AnalysisCommand> inputRecord = new TestRecord<>(new PackageURL(TEST_PURL_JACKSON_BIND), AnalysisCommand.newBuilder().setComponent(Component.newBuilder()
                .setPurl("invalid purl")).build());
        Assertions.assertThrows(StreamsException.class, () -> {
            inputTopic.pipeInput(inputRecord);
        }, "no exception thrown");

    }

    @Test
    void testNoAnalyzerApplicable() throws Exception {
        final TestRecord<PackageURL, AnalysisCommand> inputRecord = new TestRecord<>(new PackageURL("pkg:test/com.fasterxml.jackson.core/jackson-databind@2.13.4"), AnalysisCommand.newBuilder().setComponent(Component.newBuilder()
                .setPurl("pkg:test/com.fasterxml.jackson.core/jackson-databind@2.13.4")).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo("test");
                });

    }

    @Test
    @TestTransaction
    void testInternalRepositoryExternalComponent() throws MalformedPackageURLException {
        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("TYPE", "ENABLED","IDENTIFIER", "INTERNAL", "URL", "AUTHENTICATIONREQUIRED", "RESOLUTION_ORDER") VALUES
                                    ('MAVEN',true, 'central', true, 'test.com', false,1);
                """).executeUpdate();

        final TestRecord<PackageURL, AnalysisCommand> inputRecord = new TestRecord<>(new PackageURL("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4"), AnalysisCommand.newBuilder().setComponent(Component.newBuilder()
                .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4").setInternal(false)).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.MAVEN.toString().toLowerCase());
                });

    }

    @Test
    @TestTransaction
    void testExternalRepositoryInternalComponent() throws MalformedPackageURLException {
        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("TYPE", "ENABLED","IDENTIFIER", "INTERNAL", "URL", "AUTHENTICATIONREQUIRED", "RESOLUTION_ORDER") VALUES
                                    ('MAVEN',true, 'central', false, 'test.com', false,1);
                """).executeUpdate();

        final TestRecord<PackageURL, AnalysisCommand> inputRecord = new TestRecord<>(new PackageURL("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4"), AnalysisCommand.newBuilder().setComponent(Component.newBuilder()
                .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4").setInternal(true)).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.MAVEN.toString().toLowerCase());
                });

    }

    @Test
    @TestTransaction
    void testRepoMetaWithIntegrityMetaWithAuth() throws Exception {
        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("TYPE", "ENABLED","IDENTIFIER", "INTERNAL", "URL", "AUTHENTICATIONREQUIRED", "RESOLUTION_ORDER", "USERNAME", "PASSWORD") VALUES
                                    ('NPM', true, 'central', true, :url, true, 1, 'username', :encryptedPassword);
                """)
                .setParameter("encryptedPassword", secretDecryptor.encryptAsString("password"))
                .setParameter("url", String.format("http://localhost:%d", mockServer.getPort()))
                .executeUpdate();

        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/-/package/%40apollo%2Ffederation/dist-tags")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(Body.ofBinaryOrText("""
                                    {
                                        "latest": "v6.6.6"
                                    }
                                     """.getBytes(), new ContentTypeHeader(MediaType.APPLICATION_JSON)).asBytes()
                ));

        new MockServerClient("localhost", mockServer.getPort())
                .when(
                        request()
                                .withMethod("HEAD")
                                .withPath("/@apollo/federation/-/@apollo/federation-0.19.1.tgz")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("X-Checksum-MD5", "md5hash")
                );

        final TestRecord<PackageURL, AnalysisCommand> inputRecord = new TestRecord<>(new PackageURL("pkg:npm/@apollo/federation@0.19.1"),
                AnalysisCommand.newBuilder()
                        .setComponent(Component.newBuilder()
                                .setPurl("pkg:npm/@apollo/federation@0.19.1")
                                .setInternal(true))
                        .setFetchIntegrityData(true)
                        .setFetchLatestVersion(true).build());

        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.NPM.toString().toLowerCase());
                    assertThat(record.value()).isNotNull();
                    final AnalysisResult result = record.value();
                    assertThat(result.hasComponent()).isTrue();
                    assertThat(result.getRepository()).isEqualTo("central");
                    assertThat(result.getLatestVersion()).isEqualTo("v6.6.6");
                    assertThat(result.hasPublished()).isFalse();
                    assertThat(result.hasIntegrityMeta()).isTrue();
                    final var integrityMeta = result.getIntegrityMeta();
                    assertThat(integrityMeta.getMd5()).isEqualTo("md5hash");
                    assertThat(integrityMeta.getRepositoryUrl()).contains("/@apollo/federation/-/@apollo/federation-0.19.1.tgz");
                });

    }
}