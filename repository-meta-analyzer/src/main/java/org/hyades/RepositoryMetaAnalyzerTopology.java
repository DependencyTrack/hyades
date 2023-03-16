package org.hyades;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.hyades.common.KafkaTopic;
import org.hyades.processor.MetaAnalyzerProcessorSupplier;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.repometaanalysis.v1.Command;
import org.hyades.proto.repometaanalysis.v1.Component;
import org.hyades.proto.repometaanalysis.v1.Result;
import org.hyades.repositories.RepositoryAnalyzerFactory;
import org.hyades.serde.KafkaPurlSerde;

import javax.enterprise.inject.Produces;

import static org.hyades.commonutil.KafkaStreamsUtil.processorNameConsume;
import static org.hyades.commonutil.KafkaStreamsUtil.processorNameProduce;

public class RepositoryMetaAnalyzerTopology {

    @Produces
    public Topology topology(final RepositoryAnalyzerFactory analyzerFactory,
                             final MetaAnalyzerProcessorSupplier analyzerProcessorSupplier) {
        final var streamsBuilder = new StreamsBuilder();

        final var purlSerde = new KafkaPurlSerde();
        final var commandSerde = new KafkaProtobufSerde<>(Command.parser());
        final var resultSerde = new KafkaProtobufSerde<>(Result.parser());

        final KStream<PackageURL, Command> commandStream = streamsBuilder
                .stream(KafkaTopic.REPO_META_ANALYSIS_COMMAND.getName(), Consumed
                        .with(Serdes.UUID(), commandSerde)
                        .withName(processorNameConsume(KafkaTopic.REPO_META_ANALYSIS_COMMAND)))
                .filter((uuid, command) -> command.hasComponent() && isValidPurl(command.getComponent().getPurl()),
                        Named.as("filter_components_with_valid_purl"))
                // Re-key to PURL coordinates WITHOUT VERSION. As we are fetching data for packages,
                // but not specific package versions, including the version here would make our caching
                // largely ineffective. We want events for the same package to be sent to the same partition.
                .selectKey((uuid, command) -> mustParsePurlCoordinatesWithoutVersion(command.getComponent().getPurl()),
                        Named.as("re-key_to_purl_coordinates"))
                // Force a repartition to ensure that the ordering guarantees we want, based on the
                // previous re-keying operation, are effective.
                .repartition(Repartitioned
                        .with(purlSerde, commandSerde)
                        .withName("command-by-purl-coordinates"));

        commandStream
                .mapValues((purl, command) -> command.getComponent(),
                        Named.as("map_to_component"))
                .split(Named.as("applicable_analyzer"))
                .branch((purl, component) -> analyzerFactory.hasApplicableAnalyzer(purl), Branched
                        .<PackageURL, Component>withConsumer(stream -> stream
                                .processValues(analyzerProcessorSupplier, Named.as("analyze_component"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(purlSerde, resultSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "analysis_result"))))
                        .withName("-found"))
                .defaultBranch(Branched
                        .<PackageURL, Component>withConsumer(stream -> stream
                                .mapValues((purl, component) -> Result.newBuilder().setComponent(component).build(),
                                        Named.as("map_unmatched_component_to_empty_result"))
                                .to(KafkaTopic.REPO_META_ANALYSIS_RESULT.getName(), Produced
                                        .with(purlSerde, resultSerde)
                                        .withName(processorNameProduce(KafkaTopic.REPO_META_ANALYSIS_RESULT, "empty_result"))))
                        .withName("-not-found"));

        return streamsBuilder.build();
    }

    private boolean isValidPurl(final String purl) {
        try {
            new PackageURL(purl);
            return true;
        } catch (MalformedPackageURLException e) {
            return false;
        }
    }

    private PackageURL mustParsePurlCoordinatesWithoutVersion(final String purl) {
        try {
            final var parsedPurl = new PackageURL(purl);
            return new PackageURL(parsedPurl.getType(), parsedPurl.getNamespace(),
                    parsedPurl.getName(), null, null, null);
        } catch (MalformedPackageURLException e) {
            throw new RuntimeException(e);
        }
    }

}
