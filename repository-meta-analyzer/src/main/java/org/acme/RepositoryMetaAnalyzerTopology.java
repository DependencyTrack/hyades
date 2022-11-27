package org.acme;

import com.github.packageurl.PackageURL;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.acme.common.KafkaTopic;
import org.acme.commonutil.SecretsUtil;
import org.acme.model.Component;
import org.acme.model.Repository;
import org.acme.persistence.RepoEntityRepository;
import org.acme.repositories.ComposerMetaAnalyzer;
import org.acme.repositories.GemMetaAnalyzer;
import org.acme.repositories.GoModulesMetaAnalyzer;
import org.acme.repositories.HexMetaAnalyzer;
import org.acme.repositories.IMetaAnalyzer;
import org.acme.repositories.MavenMetaAnalyzer;
import org.acme.repositories.MetaModel;
import org.acme.repositories.NpmMetaAnalyzer;
import org.acme.repositories.NugetMetaAnalyzer;
import org.acme.repositories.PypiMetaAnalyzer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.acme.commonutil.KafkaStreamsUtil.processorNameConsume;
import static org.acme.commonutil.KafkaStreamsUtil.processorNameProduce;

@ApplicationScoped
public class RepositoryMetaAnalyzerTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryMetaAnalyzerTopology.class);

    private final ComposerMetaAnalyzer composerMetaAnalyzer;
    private final GemMetaAnalyzer gemMetaAnalyzer;
    private final GoModulesMetaAnalyzer goModulesMetaAnalyzer;
    private final HexMetaAnalyzer hexMetaAnalyzer;
    private final MavenMetaAnalyzer mavenMetaAnalyzer;
    private final NpmMetaAnalyzer npmMetaAnalyzer;
    private final NugetMetaAnalyzer nugetMetaAnalyzer;
    private final PypiMetaAnalyzer pypiMetaAnalyzer;
    private final RepoEntityRepository repoEntityRepository;

    @Inject
    public RepositoryMetaAnalyzerTopology(final ComposerMetaAnalyzer composerMetaAnalyzer,
                                          final GemMetaAnalyzer gemMetaAnalyzer,
                                          final GoModulesMetaAnalyzer goModulesMetaAnalyzer,
                                          final HexMetaAnalyzer hexMetaAnalyzer,
                                          final MavenMetaAnalyzer mavenMetaAnalyzer,
                                          final NpmMetaAnalyzer npmMetaAnalyzer,
                                          final NugetMetaAnalyzer nugetMetaAnalyzer,
                                          final PypiMetaAnalyzer pypiMetaAnalyzer,
                                          final RepoEntityRepository repoEntityRepository) {
        this.composerMetaAnalyzer = composerMetaAnalyzer;
        this.gemMetaAnalyzer = gemMetaAnalyzer;
        this.goModulesMetaAnalyzer = goModulesMetaAnalyzer;
        this.hexMetaAnalyzer = hexMetaAnalyzer;
        this.mavenMetaAnalyzer = mavenMetaAnalyzer;
        this.npmMetaAnalyzer = npmMetaAnalyzer;
        this.nugetMetaAnalyzer = nugetMetaAnalyzer;
        this.pypiMetaAnalyzer = pypiMetaAnalyzer;
        this.repoEntityRepository = repoEntityRepository;
    }

    @Produces
    public Topology topology() {
        final var streamsBuilder = new StreamsBuilder();
        final var componentSerde = new ObjectMapperSerde<>(Component.class);
        final var metaModelSerde = new ObjectMapperSerde<>(MetaModel.class);

        final KStream<String, Component> componentMetaAnalyzerStream = streamsBuilder
                .stream(KafkaTopic.REPO_META_ANALYSIS_COMPONENT.getName(), Consumed
                        .with(Serdes.UUID(), componentSerde)
                        .withName(processorNameConsume(KafkaTopic.REPO_META_ANALYSIS_COMPONENT)))
                .peek((uuid, component) -> LOGGER.info("Received component for repo meta analyzer: {}", component),
                        Named.as("log_component"))
                .filter((uuid, component) -> component.getPurl() != null,
                        Named.as("filter_component_without_purl"))
                .map((projectUuid, component) -> KeyValue.pair(component.getPurl().getCoordinates(), component),
                        Named.as("re-key_component_from_uuid_to_purl"))
                .peek((identifier, component) -> LOGGER.info("Re-keyed component: {} -> {}", component.getUuid(), identifier),
                        Named.as("log_re-keyed_component"));

        componentMetaAnalyzerStream.split(Named.as("meta-analysis"))
                .branch((purl, component) -> PackageURL.StandardTypes.MAVEN.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamMaven -> componentStreamMaven
                                .flatMap((uuid, component) -> performMetaAnalysis(mavenMetaAnalyzer, component),
                                        Named.as("perform_maven_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "maven_results")))
                        ).withName("-maven")
                )
                .branch((purl, component) -> PackageURL.StandardTypes.NPM.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamNpm -> componentStreamNpm
                                .flatMap((purl, component) -> performMetaAnalysis(npmMetaAnalyzer, component),
                                        Named.as("perform_npm_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "npm_results")))
                        ).withName("-npm")
                )
                .branch((purl, component) -> PackageURL.StandardTypes.HEX.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamHex -> componentStreamHex
                                .flatMap((purl, component) -> performMetaAnalysis(hexMetaAnalyzer, component),
                                        Named.as("perform_hex_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "hex_results")))
                        ).withName("-hex")
                )
                .branch((key, component) -> PackageURL.StandardTypes.PYPI.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamPypi -> componentStreamPypi
                                .flatMap((uuid, component) -> performMetaAnalysis(pypiMetaAnalyzer, component),
                                        Named.as("perform_pypi_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "pypi_results")))
                        ).withName("-pypi")
                )
                .branch((purl, component) -> PackageURL.StandardTypes.GOLANG.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamGolang -> componentStreamGolang
                                .flatMap((purl, component) -> performMetaAnalysis(goModulesMetaAnalyzer, component),
                                        Named.as("perform_golang_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "golang_results")))
                        ).withName("-golang")
                )
                .branch((purl, component) -> PackageURL.StandardTypes.NUGET.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamNuget -> componentStreamNuget
                                .flatMap((purl, component) -> performMetaAnalysis(nugetMetaAnalyzer, component),
                                        Named.as("perform_nuget_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "nuget_results")))
                        ).withName("-nuget")
                )
                .branch((purl, component) -> PackageURL.StandardTypes.COMPOSER.equals(component.getPurl().getType()),
                        Branched.<String, Component>withConsumer(componentStreamComposer -> componentStreamComposer
                                .flatMap((purl, component) -> performMetaAnalysis(composerMetaAnalyzer, component),
                                        Named.as("perform_composer_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "composer_results")))
                        ).withName("-composer")
                )
                .branch((purl, component) -> component.getPurl().getType().equalsIgnoreCase("gem"),
                        Branched.<String, Component>withConsumer(componentStreamGem -> componentStreamGem
                                .flatMap((purl, component) -> performMetaAnalysis(gemMetaAnalyzer, component),
                                        Named.as("perform_gem_meta_analysis"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "gem_results")))
                        ).withName("-gem")
                )
                .defaultBranch(Branched.<String, Component>withConsumer(componentsStreamUnknown -> componentsStreamUnknown
                                .map((purl, component) -> KeyValue.pair(component.getUuid(), new MetaModel(component)),
                                        Named.as("map_to_empty_result"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "empty_result"))))
                        .withName("-unknown")
                );

        return streamsBuilder.build();

    }

    @Transactional
    List<KeyValue<UUID, MetaModel>> performMetaAnalysis(final IMetaAnalyzer analyzer, final Component component) {
        final List<KeyValue<UUID, MetaModel>> metaModels = new ArrayList<>();

        for (Repository repository : repoEntityRepository.findRepositoryByRepositoryType(analyzer.supportedRepositoryType())) {
            if (repository.isInternal()) {
                try {
                    analyzer.setRepositoryUsernameAndPassword(repository.getUsername(), SecretsUtil.decryptAsString(repository.getPassword()));
                } catch (Exception e) {
                    LOGGER.error("Failed decrypting password for repository: " + repository.getIdentifier(), e);
                }
            }

            analyzer.setRepositoryBaseUrl(repository.getUrl());

            LOGGER.info("Performing meta analysis on component: {}", component);
            MetaModel model = analyzer.analyze(component);
            metaModels.add(KeyValue.pair(component.getUuid(), model));
        }

        return metaModels;
    }

}
