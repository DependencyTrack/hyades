package org.acme.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import org.acme.analyzer.OssIndexAnalyzer;
import org.acme.analyzer.SnykAnalyzer;
import org.acme.model.Component;
import org.acme.model.VulnerabilityResult;
import org.acme.model.VulnerabilityResultAggregate;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AnalyzerTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerTopology.class);

    private final OssIndexAnalyzer ossIndexAnalyzer;
    private final SnykAnalyzer snykAnalyzer;

    @Inject
    public AnalyzerTopology(final OssIndexAnalyzer ossIndexAnalyzer,
                            final SnykAnalyzer snykAnalyzer) {
        this.ossIndexAnalyzer = ossIndexAnalyzer;
        this.snykAnalyzer = snykAnalyzer;
    }

    @Produces
    public Topology topology() {
        final var streamsBuilder = new StreamsBuilder();

        final var componentSerde = new ObjectMapperSerde<>(Component.class);
        final var componentsSerde = Serdes.serdeFrom(new ObjectMapperSerializer<>(),
                new ObjectMapperDeserializer<List<Component>>(new TypeReference<>() {
                }));
        final var vulnResultSerde = new ObjectMapperSerde<>(VulnerabilityResult.class);
        final var vulnResultsSerde = Serdes.serdeFrom(new ObjectMapperSerializer<>(),
                new ObjectMapperDeserializer<>(new TypeReference<List<VulnerabilityResult>>() {
                }));

        // Flat-Map incoming components from the API server, and re-key the stream from UUIDs to CPEs, PURLs, and SWID Tag IDs.
        // Every component from component-analysis can thus produce up to three new events.
        final KStream<String, Component> componentStream = streamsBuilder
                .stream("component-analysis", Consumed
                        .with(Serdes.UUID(), componentSerde)
                        .withName("consume_component-analysis_topic"))
                .peek((uuid, component) -> LOGGER.info("Received component: {}", component),
                        Named.as("log_components"))
                .flatMap((projectUuid, component) -> {
                    final var components = new ArrayList<KeyValue<String, Component>>();
                    if (component.getCpe() != null) {
                        // TODO: Canonicalize the CPE used as key, so that CPEs describing the same component end up in the same partition.
                        components.add(KeyValue.pair(component.getCpe(), component));
                    }
                    if (component.getPurl() != null) {
                        components.add(KeyValue.pair(component.getPurl().getCoordinates(), component));
                    }
                    if (component.getSwidTagId() != null) {
                        // NOTE: Barely any components have a SWID Tag ID yet, and no scanner supports it
                        components.add(KeyValue.pair(component.getSwidTagId(), component));
                    }
                    if (component.getCpe() == null && component.getPurl() == null && component.getSwidTagId() == null) {
                        components.add(KeyValue.pair("no-identifier", component));
                    }
                    return components;
                }, Named.as("re-key_components_from_uuid_to_identifiers"))
                .peek((identifier, component) -> LOGGER.info("Re-keyed component: {} -> {}", component.getUuid(), identifier),
                        Named.as("log_re-keyed_components"));

        // Consume from the topic where analyzers write their results to.
        final KStream<String, VulnerabilityResult> analyzerResultStream = streamsBuilder
                .stream("component-analysis-vuln", Consumed
                        .with(Serdes.String(), vulnResultSerde)
                        .withName("consume_component-analysis-vuln_topic"));

        // Re-key all vulnerability results from CPE/PURL/SWID back to component UUIDs.
        analyzerResultStream
                .peek((identifier, vulnResult) -> LOGGER.info("Re-keying result: {} -> {}", identifier, vulnResult.getComponent().getUuid()),
                        Named.as("log_vuln_results_re-keying"))
                .map((identifier, vulnResult) -> KeyValue.pair(vulnResult.getComponent().getUuid(), vulnResult),
                        Named.as("re-key_vuln_results_from_identifier_to_component_uuid"))
                .to("component-vuln-analysis-result", Produced
                        .with(Serdes.UUID(), vulnResultSerde)
                        .withName("produce_to_component-vuln-analysis-result_topic"));

        // Aggregate vulnerability results in a KTable.
        // This will map identifiers (CPE, PURL, SWID Tag ID) to identified vulnerabilities.
        final KTable<String, List<VulnerabilityResult>> cacheTable = analyzerResultStream
                .groupByKey(Grouped.as("group_by_component_identifier"))
                .aggregate(ArrayList::new,
                        (identity, result, aggregate) -> {
                            // Component details are not required at this level, because the vulnerabilities
                            // are only associated with identifiers.
                            // TODO: Maybe use a separate model class to make this more clear?
                            result.setComponent(null);

                            aggregate.add(result);
                            return aggregate;
                        }, Named.as("aggregate_vuln_results_for_component_identifier"),
                        Materialized.<String, List<VulnerabilityResult>, KeyValueStore<Bytes, byte[]>>as("vuln_results_cache")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(vulnResultsSerde));

        // Left-Join the stream of components with the cacheTable constructed above.
        final KStream<String, VulnerabilityResultAggregate> componentCacheJoinStream = componentStream
                .leftJoin(cacheTable, (component, vulnerabilityResults) -> {
                            final var aggregate = new VulnerabilityResultAggregate();
                            aggregate.setComponent(component);
                            if (vulnerabilityResults != null) {
                                aggregate.setVulnerabilityResults(vulnerabilityResults);
                            }
                            return aggregate;
                        },
                        Joined.with(Serdes.String(), componentSerde, vulnResultsSerde)
                                .withName("component_vuln-results-cache_join"))
                .peek((identifier, vulnResultAggregate) -> LOGGER.info("Joined: {} -> {}", identifier, vulnResultAggregate),
                        Named.as("log_join_result"));

        componentCacheJoinStream
                .split(Named.as("vuln-result-cache"))
                .branch(
                        (identifier, vulnResultAggregate) -> vulnResultAggregate.getVulnerabilityResults() != null
                                && vulnResultAggregate.getVulnerabilityResults().size() > 0,
                        Branched.<String, VulnerabilityResultAggregate>withConsumer(vulnResultAggregateStream -> vulnResultAggregateStream
                                .peek((identifier, vulnResultAggregate) -> LOGGER.info("Cache hit: {}", identifier),
                                        Named.as("log_cache_hit"))
                                .flatMapValues((identifier, vulnResultAggregate) -> vulnResultAggregate.getVulnerabilityResults().stream()
                                        .peek(vulnResult -> vulnResult.setComponent(vulnResultAggregate.getComponent()))
                                        .toList(), Named.as("populate_component_field_of_vuln_results"))
                                .map((identifier, vulnResult) -> KeyValue.pair(vulnResult.getComponent().getUuid(), vulnResult),
                                        Named.as("re-key_cached_vuln_results_from_identifier_to_component_uuid"))
                                .to("component-vuln-analysis-result", Produced
                                        .with(Serdes.UUID(), vulnResultSerde)
                                        .withName("produce_cached_vuln_results_to_component-vuln-analysis-result_topic"))
                        ).withName("-hit")
                )
                .defaultBranch(
                        Branched.<String, VulnerabilityResultAggregate>withConsumer(missed -> {
                            // Branch off component events to different topics, based on their identifiers.
                            final Map<String, KStream<String, Component>> branches = missed
                                    .peek((identifier, vulnResultAggregate) -> LOGGER.info("Cache miss: {}", identifier),
                                            Named.as("log_cache_miss"))
                                    .mapValues(VulnerabilityResultAggregate::getComponent, Named.as("map_to_component"))
                                    .split(Named.as("component-with-identifier-type"))
                                    .branch((identifier, component) -> isCpe(identifier), Branched.as("-cpe"))
                                    .branch((identifier, component) -> isPurl(identifier), Branched.as("-purl"))
                                    .branch((identifier, component) -> isSwidTagId(identifier), Branched.as("-swid"))
                                    .defaultBranch(Branched.as("-unknown"));
                            branches.get("component-with-identifier-type-cpe").to("component-analysis-cpe", Produced
                                    .with(Serdes.String(), componentSerde)
                                    .withName("produce_to_component-analysis-cpe_topic"));
                            branches.get("component-with-identifier-type-purl").to("component-analysis-purl", Produced
                                    .with(Serdes.String(), componentSerde)
                                    .withName("produce_to_component-analysis-purl_topic"));
                            branches.get("component-with-identifier-type-swid").to("component-analysis-swid", Produced
                                    .with(Serdes.String(), componentSerde)
                                    .withName("produce_to_component-analysis-swid_topic"));
                            branches.get("component-with-identifier-type-unknown")
                                    // The component does not have an identifier that we can work with,
                                    // but we still want to produce a result.
                                    // TODO: Instead of reporting "no vulnerability", report "not applicable" or so
                                    .map((identifier, component) -> {
                                        final var result = new VulnerabilityResult();
                                        result.setComponent(component);
                                        result.setIdentity(null);
                                        result.setVulnerability(null);
                                        return KeyValue.pair(component.getUuid(), result);
                                    }, Named.as("map_to_empty_vuln_result"))
                                    .to("component-vuln-analysis-result", Produced
                                            .with(Serdes.UUID(), vulnResultSerde)
                                            .withName("produce_empty_result_to_component-vuln-analysis-result_topic"));
                        }).withName("-miss")
                );

        final KStream<String, Component> purlComponentStream = streamsBuilder
                .stream("component-analysis-purl", Consumed
                        .with(Serdes.String(), componentSerde)
                        .withName("consume_from_component-analysis-purl_topic"));

        // Perform a time window based aggregation of components with PURLs.
        // TODO: Repeat this for CPE and SWID Tag ID
        final KStream<Windowed<Integer>, List<Component>> purlAggregateStream = purlComponentStream
                // Windowing and aggregation requires records to have the same key.
                // Because most records will have different identifiers as key, we re-key
                // the stream to the partition ID the records are in.
                // This allows us to aggregate all records within the partition(s) we're consuming from.
                .transform(PartitionIdReKeyTransformer::new, Named.as("re-key_components_from_purl_to_partition_id"))
                .groupByKey(Grouped.with(Serdes.Integer(), componentSerde)
                        .withName("group_by_partition_id"))
                .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofSeconds(3), Duration.ofSeconds(1))) // TODO: Tweak this?
                .aggregate(ArrayList::new,
                        (partition, component, aggregate) -> {
                            aggregate.add(component);
                            return aggregate;
                        }, Named.as("aggregate_components_with_purl"),
                        Materialized.<Integer, List<Component>, WindowStore<Bytes, byte[]>>as("aggregated_components_with_purl")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(componentsSerde))
                // We're only interested in the final aggregation result of the window
                // when it closes. Per default though, all updates to the aggregate are
                // streamed to the changelog topic, causing too many and largely duplicated
                // "batches":
                //  --> [A]
                //  --> [A B]
                //  --> [A B C]
                // Instead, we suppress all updates to the changelog topic until the window closed.
                .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded())
                        .withName("suppress_aggregates_until_time_window_closes"))
                .toStream(Named.as("stream_component_aggregates"));

        if (ossIndexAnalyzer.isEnabled()) {
            // Perform the OSS Index analysis based on the PURL aggregates.
            purlAggregateStream
                    .flatMap((window, components) -> analyzeOssIndex(components),
                            Named.as("analyze_with_ossindex"))
                    .to("component-analysis-vuln", Produced
                            .with(Serdes.String(), vulnResultSerde)
                            .withName("produce_ossindex_results_to_component-analysis-vuln_topic"));
        }

        if (snykAnalyzer.isEnabled()) {
            // Snyk does not benefit from batching yet, so consume from the non-aggregated stream directly.
            purlComponentStream
                    .flatMap((purl, component) -> analyzeSnyk(component),
                            Named.as("analyze_with_snyk"))
                    .to("component-analysis-vuln", Produced
                            .with(Serdes.String(), vulnResultSerde)
                            .withName("produce_snyk_results_to_component-analysis-vuln_topic"));
        }

        return streamsBuilder.build();
    }

    private boolean isPurl(final String purl) {
        try {
            new PackageURL(purl);
            return true;
        } catch (MalformedPackageURLException e) {
            return false;
        }
    }

    private boolean isCpe(final String cpe) {
        return StringUtils.startsWith(cpe, "cpe:");
    }

    private boolean isSwidTagId(final String swidTagId) {
        return false;
    }

    private List<KeyValue<String, VulnerabilityResult>> analyzeOssIndex(final List<Component> components) {
        LOGGER.info("Performing OSS Index analysis for {} components: {}", components.size(), components);
        return ossIndexAnalyzer.analyze(components).stream() // TODO: Handle exceptions
                .map(vulnResult -> KeyValue.pair(vulnResult.getComponent().getPurl().getCoordinates(), vulnResult))
                .toList();
    }

    private List<KeyValue<String, VulnerabilityResult>> analyzeSnyk(final Component component) {
        LOGGER.info("Performing Snyk analysis for component: {}", component);
        return snykAnalyzer.analyze(List.of(component)).stream() // TODO: Handle exceptions
                .map(vulnResult -> KeyValue.pair(vulnResult.getComponent().getPurl().getCoordinates(), vulnResult))
                .toList();
    }

}
