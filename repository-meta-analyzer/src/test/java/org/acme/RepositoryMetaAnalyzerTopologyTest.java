package org.acme;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.common.KafkaTopic;
import org.acme.model.Component;
import org.acme.model.MetaAnalyzerCacheKey;
import org.acme.model.Repository;
import org.acme.persistence.RepoEntityRepository;
import org.acme.repositories.ComposerMetaAnalyzer;
import org.acme.repositories.GemMetaAnalyzer;
import org.acme.repositories.GoModulesMetaAnalyzer;
import org.acme.repositories.HexMetaAnalyzer;
import org.acme.repositories.MavenMetaAnalyzer;
import org.acme.model.MetaModel;
import org.acme.repositories.NpmMetaAnalyzer;
import org.acme.repositories.NugetMetaAnalyzer;
import org.acme.repositories.PypiMetaAnalyzer;
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

import javax.cache.Cache;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class RepositoryMetaAnalyzerTopologyTest {

    @Inject
    Topology topology;

    @Inject
    @Named("metaAnalyzerCache")
    Cache<MetaAnalyzerCacheKey, MetaModel> cache;

    private TopologyTestDriver testDriver;
    private TestInputTopic<UUID, Component> inputTopic;
    private TestOutputTopic<UUID, MetaModel> outputTopic;

    private ComposerMetaAnalyzer composerMetaAnalyzerMock;
    private GemMetaAnalyzer gemMetaAnalyzerMock;
    private GoModulesMetaAnalyzer goModulesMetaAnalyzerMock;
    private HexMetaAnalyzer hexMetaAnalyzerMock;
    private MavenMetaAnalyzer mavenMetaAnalyzerMock;
    private NpmMetaAnalyzer npmMetaAnalyzerMock;
    private NugetMetaAnalyzer nugetMetaAnalyzerMock;
    private PypiMetaAnalyzer pypiMetaAnalyzerMock;
    private RepoEntityRepository repoEntityRepositoryMock;

    @BeforeEach
    void beforeEach() {
        composerMetaAnalyzerMock = Mockito.mock(ComposerMetaAnalyzer.class);
        QuarkusMock.installMockForType(composerMetaAnalyzerMock, ComposerMetaAnalyzer.class);

        gemMetaAnalyzerMock = Mockito.mock(GemMetaAnalyzer.class);
        QuarkusMock.installMockForType(gemMetaAnalyzerMock, GemMetaAnalyzer.class);

        goModulesMetaAnalyzerMock = Mockito.mock(GoModulesMetaAnalyzer.class);
        QuarkusMock.installMockForType(goModulesMetaAnalyzerMock, GoModulesMetaAnalyzer.class);

        hexMetaAnalyzerMock = Mockito.mock(HexMetaAnalyzer.class);
        QuarkusMock.installMockForType(hexMetaAnalyzerMock, HexMetaAnalyzer.class);

        mavenMetaAnalyzerMock = Mockito.mock(MavenMetaAnalyzer.class);
        QuarkusMock.installMockForType(mavenMetaAnalyzerMock, MavenMetaAnalyzer.class);

        npmMetaAnalyzerMock = Mockito.mock(NpmMetaAnalyzer.class);
        QuarkusMock.installMockForType(npmMetaAnalyzerMock, NpmMetaAnalyzer.class);

        nugetMetaAnalyzerMock = Mockito.mock(NugetMetaAnalyzer.class);
        QuarkusMock.installMockForType(nugetMetaAnalyzerMock, NugetMetaAnalyzer.class);

        pypiMetaAnalyzerMock = Mockito.mock(PypiMetaAnalyzer.class);
        QuarkusMock.installMockForType(pypiMetaAnalyzerMock, PypiMetaAnalyzer.class);

        repoEntityRepositoryMock = Mockito.mock(RepoEntityRepository.class);
        QuarkusMock.installMockForType(repoEntityRepositoryMock, RepoEntityRepository.class);

        testDriver = new TopologyTestDriver(topology);
        inputTopic = testDriver.createInputTopic(KafkaTopic.REPO_META_ANALYSIS_COMPONENT.getName(), new UUIDSerializer(), new ObjectMapperSerializer<>());
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
        Mockito.when(repoEntityRepositoryMock.findRepositoryByRepositoryType(any())).thenReturn(List.of(repository));

        // mock maven analyzer analyze method
        final MetaModel outputMetaModel = new MetaModel();
        outputMetaModel.setLatestVersion("test");
        Mockito.when(mavenMetaAnalyzerMock.analyze(any())).thenReturn(outputMetaModel);

        inputTopic.pipeInput(uuid, component);
        Assertions.assertNotNull(cache.get(new MetaAnalyzerCacheKey(mavenMetaAnalyzerMock.getName(), component.getPurl().canonicalize())));
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
        Mockito.when(repoEntityRepositoryMock.findRepositoryByRepositoryType(any())).thenReturn(List.of(repository));

        // mock maven analyzer analyze method
        final MetaModel outputMetaModel = new MetaModel();
        outputMetaModel.setLatestVersion("test");

        // populate the cache to hit the match
        cache.put(new MetaAnalyzerCacheKey(mavenMetaAnalyzerMock.getName(), component.getPurl().canonicalize()), outputMetaModel);

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
        cache.clear();
    }
}

