package org.hyades;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.hyades.common.KafkaTopic;
import org.hyades.model.Component;
import org.hyades.model.MetaAnalyzerCacheKey;
import org.hyades.model.Repository;
import org.hyades.persistence.RepoEntityRepository;
import org.hyades.repositories.ComposerMetaAnalyzer;
import org.hyades.repositories.GemMetaAnalyzer;
import org.hyades.repositories.GoModulesMetaAnalyzer;
import org.hyades.repositories.HexMetaAnalyzer;
import org.hyades.repositories.MavenMetaAnalyzer;
import org.hyades.model.MetaModel;
import org.hyades.repositories.NpmMetaAnalyzer;
import org.hyades.repositories.NugetMetaAnalyzer;
import org.hyades.repositories.PypiMetaAnalyzer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class RepositoryMetaAnalyzerTopologyTest {

    @Inject
    Topology topology;

    @Inject
    @CacheName("metaAnalyzer")
    Cache cache;

    private TopologyTestDriver testDriver;
    private TestInputTopic<UUID, Component> inputTopic;
    private TestOutputTopic<UUID, MetaModel> outputTopic;

    @InjectMock
    private ComposerMetaAnalyzer composerMetaAnalyzerMock;
    @InjectMock
    private GemMetaAnalyzer gemMetaAnalyzerMock;
    @InjectMock
    private GoModulesMetaAnalyzer goModulesMetaAnalyzerMock;

    @InjectMock
    private HexMetaAnalyzer hexMetaAnalyzerMock;

    @InjectMock
    private MavenMetaAnalyzer mavenMetaAnalyzerMock;

    @InjectMock
    private NpmMetaAnalyzer npmMetaAnalyzerMock;

    @InjectMock
    private NugetMetaAnalyzer nugetMetaAnalyzerMock;

    @InjectMock
    private PypiMetaAnalyzer pypiMetaAnalyzerMock;

    @InjectMock
    private RepoEntityRepository repoEntityRepositoryMock;

    @BeforeEach
    void beforeEach() {
        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.REPO_META_ANALYSIS_COMMAND.getName(), new UUIDSerializer(), new ObjectMapperSerializer<>());
        outputTopic = testDriver.createOutputTopic(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), new UUIDDeserializer(), new ObjectMapperDeserializer<>(MetaModel.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAnalyzerCacheMiss() {

        final var component = new Component();
        final UUID uuid = UUID.randomUUID();
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:9.1.1");
        component.setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");
        component.setSwidTagId("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiID8");

        // mock repository data
        Repository repository = new Repository();
        repository.setInternal(false);
        repository.setUrl("https://repo1.maven.org/maven2/");
        //Mockito.when(repoEntityRepositoryMock.findRepositoryByRepositoryType(any())).thenReturn(List.of(repository));

        // mock maven analyzer analyze method
        final MetaModel outputMetaModel = new MetaModel();
        outputMetaModel.setLatestVersion("test");
        Mockito.when(mavenMetaAnalyzerMock.analyze(any())).thenReturn(outputMetaModel);

        inputTopic.pipeInput(uuid, component);
        //Assertions.assertNotNull(cache.as(CaffeineCache.class).getIfPresent(new MetaAnalyzerCacheKey(mavenMetaAnalyzerMock.getName(), component.getPurl().canonicalize())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAnalyzerCacheHit() {

        final var component = new Component();
        final UUID uuid = UUID.randomUUID();
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:9.1.1");
        component.setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");
        component.setSwidTagId("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiID8");

        // mock repository data
        Repository repository = new Repository();
        repository.setInternal(false);
        repository.setUrl("https://repo1.maven.org/maven2/");
        //Mockito.when(repoEntityRepositoryMock.findRepositoryByRepositoryType(any())).thenReturn(List.of(repository));

        // mock maven analyzer analyze method
        final MetaModel outputMetaModel = new MetaModel();
        outputMetaModel.setLatestVersion("test");

        // populate the cache to hit the match
        //cache.as(CaffeineCache.class).put(new MetaAnalyzerCacheKey(mavenMetaAnalyzerMock.getName(), component.getPurl().canonicalize()), CompletableFuture.completedFuture(outputMetaModel));

        inputTopic.pipeInput(uuid, component);
        final KeyValue<UUID, MetaModel> record = outputTopic.readKeyValue();
        Assertions.assertEquals("test", record.value.getLatestVersion());
    }


    @Test
    void testPerformMetaAnalysis(){
        final var component = new Component();
        final UUID uuid = UUID.randomUUID();
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:9.1.1");
        component.setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");
        component.setSwidTagId("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiID8");
        inputTopic.pipeInput(uuid, component);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
    }


    @Test
    void testNoPurlComponent(){
        final var component = new Component();
        final UUID uuid = UUID.randomUUID();
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:9.1.1");
        component.setSwidTagId("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiID8");
        inputTopic.pipeInput(uuid, component);

        Assertions.assertEquals(0, outputTopic.getQueueSize());
    }

    @Test
    void testMetaOutput(){
        final var component = new Component();
        final UUID uuid = UUID.randomUUID();
        component.setUuid(uuid);
        component.setCpe("cpe:/a:acme:application:9.1.1");
        component.setPurl("pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.13.2");
        component.setSwidTagId("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiID8");
        inputTopic.pipeInput(uuid, component);

        Assertions.assertEquals(1, outputTopic.getQueueSize());
        final KeyValue<UUID, MetaModel> record = outputTopic.readKeyValue();
        Assertions.assertEquals(uuid, record.key);
        Assertions.assertEquals(uuid, record.value.getComponent().getUuid());
        Assertions.assertNull(record.value.getLatestVersion());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        cache.invalidateAll();
    }
}

