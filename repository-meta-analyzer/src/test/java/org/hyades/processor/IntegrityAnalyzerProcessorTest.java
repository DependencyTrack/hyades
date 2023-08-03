package org.hyades.processor;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
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
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.proto.repometaanalysis.v1.HashMatchStatus;
import org.hyades.proto.repometaanalysis.v1.IntegrityResult;
import org.hyades.repositories.IntegrityAnalyzerFactory;
import org.hyades.serde.KafkaPurlSerde;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(MetaAnalyzerProcessorTest.TestProfile.class)
class IntegrityAnalyzerProcessorTest {

    public static class TestProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.kafka.snappy.enabled", "false"
            );
        }
    }

    private static final String TEST_PURL_JACKSON_BIND = "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4";

    private TopologyTestDriver testDriver;
    private TestInputTopic<PackageURL, Component> inputTopic;
    private TestOutputTopic<PackageURL, IntegrityResult> outputTopic;
    @Inject
    RepoEntityRepository repoEntityRepository;

    @Inject
    IntegrityAnalyzerFactory analyzerFactory;

    @Inject
    EntityManager entityManager;

    @Inject
    @CacheName("integrityAnalyzer")
    Cache cache;

    @Inject
    SecretDecryptor secretDecryptor;

    @BeforeEach
    void beforeEach() {
        final var processorSupplier = new IntegrityAnalyzerProcessorSupplier(repoEntityRepository, analyzerFactory, secretDecryptor, cache);

        final var valueSerde = new KafkaProtobufSerde<>(Component.parser());
        final var purlSerde = new KafkaPurlSerde();
        final var valueSerdeResult = new KafkaProtobufSerde<>(IntegrityResult.parser());

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

    @Test
    void testWithNoSupportedRepositoryTypes() throws Exception {
        final TestRecord<PackageURL, Component> inputRecord = new TestRecord<>(new PackageURL(TEST_PURL_JACKSON_BIND), Component.newBuilder()
                .setPurl(TEST_PURL_JACKSON_BIND).setInternal(false).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.MAVEN.toString().toLowerCase(),
                            assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.UNKNOWN.getNumber()),
                            assertThat(record.value().getSha1HashMatchValue()).isEqualTo(HashMatchStatus.UNKNOWN.getNumber()),
                            assertThat(record.value().getSha256MatchValue()).isEqualTo(HashMatchStatus.UNKNOWN.getNumber()));
                });
    }

    @Test
    void testMalformedPurl() throws Exception {
        final TestRecord<PackageURL, Component> inputRecord = new TestRecord<>(new PackageURL(TEST_PURL_JACKSON_BIND), Component.newBuilder()
                .setPurl("invalid purl").build());
        Assertions.assertThrows(StreamsException.class, () -> {
            inputTopic.pipeInput(inputRecord);
        }, "no exception thrown");

    }

    @Test
    void testNoAnalyzerApplicable() throws Exception {
        final TestRecord<PackageURL, Component> inputRecord = new TestRecord<>(new PackageURL("pkg:test/com.fasterxml.jackson.core/jackson-databind@2.13.4"), Component.newBuilder()
                .setPurl("pkg:test/com.fasterxml.jackson.core/jackson-databind@2.13.4").build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo("test");
                });

    }

    @Test
    @TestTransaction
    void testIntegrityCheckEnabled() throws MalformedPackageURLException {
        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("TYPE", "ENABLED","IDENTIFIER", "INTERNAL", "URL", "AUTHENTICATIONREQUIRED", "INTEGRITYCHECKENABLED", "RESOLUTION_ORDER") VALUES
                                    ('MAVEN',true, 'central', false, 'test.com', false, true,1);
                """).executeUpdate();

        final TestRecord<PackageURL, Component> inputRecord = new TestRecord<>(new PackageURL("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4"), Component.newBuilder()
                .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4").setInternal(false).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.MAVEN.toString().toLowerCase());
                    assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.COMPONENT_MISSING_HASH.getNumber());
                    assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.COMPONENT_MISSING_HASH.getNumber());
                    assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.COMPONENT_MISSING_HASH.getNumber());
                });

    }

    @Test
    @TestTransaction
    void testIntegrityCheckDisabled() throws MalformedPackageURLException {
        entityManager.createNativeQuery("""
                INSERT INTO "REPOSITORY" ("TYPE", "ENABLED","IDENTIFIER", "INTERNAL", "URL", "AUTHENTICATIONREQUIRED", "INTEGRITYCHECKENABLED", "RESOLUTION_ORDER") VALUES
                                    ('MAVEN',true, 'central', false, 'test.com', false, false,1);
                """).executeUpdate();

        final TestRecord<PackageURL, Component> inputRecord = new TestRecord<>(new PackageURL("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4"), Component.newBuilder()
                .setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.4").setInternal(false).build());
        inputTopic.pipeInput(inputRecord);
        assertThat(outputTopic.getQueueSize()).isEqualTo(1);
        assertThat(outputTopic.readRecordsToList()).satisfiesExactly(
                record -> {
                    assertThat(record.key().getType()).isEqualTo(RepositoryType.MAVEN.toString().toLowerCase());
                    assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.UNKNOWN.getNumber());
                    assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.UNKNOWN.getNumber());
                    assertThat(record.value().getMd5HashMatchValue()).isEqualTo(HashMatchStatus.UNKNOWN.getNumber());
                });

    }
}