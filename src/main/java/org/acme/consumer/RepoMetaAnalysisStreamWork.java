package org.acme.consumer;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import org.acme.model.Component;
import org.acme.repositories.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.UUID;

@ApplicationScoped
public class RepoMetaAnalysisStreamWork {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoMetaAnalysisStreamWork.class);


    public void createStructure(StreamsBuilder streamsBuilder, MavenMetaAnalyzer mavenMetaAnalyzer,
                                GoModulesMetaAnalyzer goModulesMetaAnalyzer,
                                HexMetaAnalyzer hexMetaAnalyzer,
                                NpmMetaAnalyzer npmMetaAnalyzer,
                                NugetMetaAnalyzer nugetMetaAnalyzer,
                                PypiMetaAnalyzer pypiMetaAnalyzer,
                                GemMetaAnalyzer gemMetaAnalyzer,

                                ComposerMetaAnalyzer composerMetaAnalyzer) {
        final var componentSerde = new ObjectMapperSerde<>(Component.class);
        final var metaModelSerde = new ObjectMapperSerde<>(MetaModel.class);
        final KStream<String, Component> componentMetaAnalyzerStream = streamsBuilder
                .stream("repo-meta-analysis", Consumed
                        .with(Serdes.UUID(), componentSerde)
                        .withName("consume_component_meta_analysis_topic"))
                .peek((uuid, component) -> LOGGER.info("Received component for repo meta analyzer: {}", component),
                        Named.as("log_components_repo_meta"))
                .filter((uuid, component) -> component.getPurl() != null, Named.as("filter_components_for_not_null_purl"))
                .flatMap((projectUuid, component) -> {
                    final var components = new ArrayList<KeyValue<String, Component>>();
                    //Check if purl is not null on producer (dt) side
                    components.add(KeyValue.pair(component.getPurl().getCoordinates(), component));

                    return components;
                }, Named.as("re-key_components_from_uuid_to_purl_for_meta"))
                .peek((identifier, component) -> LOGGER.info("Re-keyed component: {} -> {}", component.getUuid(), identifier),
                        Named.as("log_re-keyed_components_for_meta"));

        //creating the branches needed for individual analysis
        String metaAnalysisResultTopic = "component-meta-analysis-result";
        componentMetaAnalyzerStream.split(Named.as("meta-analysis"))
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("maven"),
                        Branched.<String, Component>withConsumer(componentStreamMaven -> componentStreamMaven.peek((identifier, component) ->
                                        LOGGER.info("Component sending to maven meta analyzer"), Named.as("maven_meta_analyzer"))
                                .map((uuid, component) -> mavenMetaAnalysis(component, mavenMetaAnalyzer), Named.as("performing_maven_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_maven_meta_analysis_result"))
                        ).withName("-maven-analyzer")
                )
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("npm"),
                        Branched.<String, Component>withConsumer(componentStreamNpm -> componentStreamNpm.peek((identifier, component) ->
                                        LOGGER.info("Component sending to maven meta analyzer"), Named.as("npm_meta_analyzer"))
                                .map((uuid, component) -> npmMetaAnalysis(component, npmMetaAnalyzer), Named.as("performing_npm_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_npm_meta_analysis_result"))
                        ).withName("-npm-analyzer")
                )
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("hex"),
                        Branched.<String, Component>withConsumer(componentStreamHex -> componentStreamHex.peek((identifier, component) ->
                                        LOGGER.info("Component sending to hex meta analyzer"), Named.as("hex_meta_analyzer"))
                                .map((uuid, component) -> hexMetaAnalysis(component, hexMetaAnalyzer), Named.as("performing_hex_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_hex_meta_analysis_result"))
                        ).withName("-hex-analyzer"))
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("pypi"),
                        Branched.<String, Component>withConsumer(componentStreamPypi -> componentStreamPypi.peek((identifier, component) ->
                                        LOGGER.info("Component sending to pypi meta analyzer"), Named.as("pypi_meta_analyzer"))
                                .map((uuid, component) -> pypiMetaAnalysis(component, pypiMetaAnalyzer), Named.as("performing_pypi_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_pypi_meta_analysis_result"))
                        ).withName("-pypi-analyzer"))
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("golang"),
                        Branched.<String, Component>withConsumer(componentStreamGolang -> componentStreamGolang.peek((identifier, component) ->
                                        LOGGER.info("Component sending to golang meta analyzer"), Named.as("golang_meta_analyzer"))
                                .map((uuid, component) -> goMetaAnalysis(component, goModulesMetaAnalyzer), Named.as("performing_go_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_golang_meta_analysis_result"))
                        ).withName("-golang-analyzer"))
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("nuget"),
                        Branched.<String, Component>withConsumer(componentStreamNuget -> componentStreamNuget.peek((identifier, component) ->
                                        LOGGER.info("Component sending to nuget meta analyzer"), Named.as("nuget_meta_analyzer"))
                                .map((uuid, component) -> nugetMetaAnalysis(component, nugetMetaAnalyzer), Named.as("performing_nuget_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_nuget_meta_analysis_result"))
                        ).withName("-nuget-analyzer"))
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("composer"),
                        Branched.<String, Component>withConsumer(componentStreamComposer -> componentStreamComposer.peek((identifier, component) ->
                                        LOGGER.info("Component sending to composer meta analyzer"), Named.as("composer_meta_analyzer"))
                                .map((uuid, component) -> composerMetaAnalysis(component, composerMetaAnalyzer), Named.as("performing_composer_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_composer_meta_analysis_result"))
                        ).withName("-composer-analyzer"))
                .branch((key, component) -> component.getPurl().getType().equalsIgnoreCase("gem"),
                        Branched.<String, Component>withConsumer(componentStreamGem -> componentStreamGem.peek((identifier, component) ->
                                        LOGGER.info("Component sending to gem meta analyzer"), Named.as("gem_meta_analyzer"))
                                .map((uuid, component) -> gemMetaAnalysis(component, gemMetaAnalyzer), Named.as("performing_gem_meta_analysis"))
                                .to(metaAnalysisResultTopic, Produced
                                        .with(Serdes.UUID(), metaModelSerde)
                                        .withName("adding_gem_meta_analysis_result"))
                        ).withName("-gem-analyzer"));

    }

    private KeyValue<UUID, MetaModel> mavenMetaAnalysis(final Component component, MavenMetaAnalyzer mavenMetaAnalyzer) {
        LOGGER.info("Performing maven meta analysis on component: {}", component);
        MetaModel model = mavenMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> pypiMetaAnalysis(final Component component, PypiMetaAnalyzer pypiMetaAnalyzer) {
        LOGGER.info("Performing pypi meta analysis on component: {}", component);
        MetaModel model = pypiMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> goMetaAnalysis(final Component component, GoModulesMetaAnalyzer goModulesMetaAnalyzer) {
        LOGGER.info("Performing golang meta analysis on component: {}", component);
        MetaModel model = goModulesMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> nugetMetaAnalysis(final Component component, NugetMetaAnalyzer nugetMetaAnalyzer) {
        LOGGER.info("Performing nuget meta analysis on component: {}", component);
        MetaModel model = nugetMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> npmMetaAnalysis(final Component component, NpmMetaAnalyzer npmMetaAnalyzer) {
        LOGGER.info("Performing npm meta analysis on component: {}", component);
        MetaModel model = npmMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> hexMetaAnalysis(final Component component, HexMetaAnalyzer hexMetaAnalyzer) {
        LOGGER.info("Performing hex meta analysis on component: {}", component);
        MetaModel model = hexMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> gemMetaAnalysis(final Component component, GemMetaAnalyzer gemMetaAnalyzer) {
        LOGGER.info("Performing gem meta analysis on component: {}", component);
        MetaModel model = gemMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

    private KeyValue<UUID, MetaModel> composerMetaAnalysis(final Component component, ComposerMetaAnalyzer composerMetaAnalyzer) {
        LOGGER.info("Performing composer meta analysis on component: {}", component);
        MetaModel model = composerMetaAnalyzer.analyze(component);
        return KeyValue.pair(component.getUuid(), model);
    }

}
