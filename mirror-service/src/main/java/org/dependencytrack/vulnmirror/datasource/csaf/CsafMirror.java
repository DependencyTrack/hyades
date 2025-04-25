/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.vulnmirror.datasource.csaf;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.csaf.retrieval.CsafLoader;
import io.csaf.retrieval.RetrievedAggregator;
import io.csaf.retrieval.RetrievedProvider;
import io.csaf.schema.generated.Csaf;
import io.csaf.schema.generated.Provider;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import kotlinx.serialization.json.Json;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.cyclonedx.proto.v1_6.Bom;
import org.dependencytrack.common.KafkaTopic;
import org.dependencytrack.persistence.model.CsafSourceEntity;
import org.dependencytrack.persistence.repository.CsafSourceRepository;
import org.dependencytrack.proto.mirror.v1.CsafDocumentItem;
import org.dependencytrack.vulnmirror.datasource.AbstractDatasourceMirror;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;
import static org.dependencytrack.vulnmirror.datasource.csaf.CsafToCdxParser.computeVulnerabilityId;

@ApplicationScoped
public class CsafMirror extends AbstractDatasourceMirror<CsafMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsafMirror.class);
    private static final String NOTIFICATION_TITLE = "CSAF Mirroring";

    private final CsafConfig config;
    private final CsafLoaderFactory csafLoaderFactory;
    private final ExecutorService executorService;
    private final CsafSourceRepository csafSourceRepository;
    private final Timer durationTimer;
    private final Producer<String, byte[]> kafkaProducer;

    CsafMirror(final CsafLoaderFactory csafLoaderFactory,
            final CsafConfig config,
            @ForCsafMirror final ExecutorService executorService,
            final CsafSourceRepository csafSourceRepository,
            final MirrorStateStore mirrorStateStore,
            final VulnerabilityDigestStore vulnDigestStore,
            final Producer<String, byte[]> kafkaProducer,
            @ForCsafMirror final Timer durationTimer) {
        super(Datasource.CSAF, mirrorStateStore, vulnDigestStore, kafkaProducer, CsafMirrorState.class);
        this.config = config;
        this.executorService = executorService;
        this.csafSourceRepository = csafSourceRepository;
        this.csafLoaderFactory = csafLoaderFactory;
        this.durationTimer = durationTimer;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public boolean supportsDatasource(final Datasource datasource) {
        return datasource == Datasource.CSAF;
    }

    @Override
    public Future<?> doMirror() {
        if (!config.enabled().orElse(false)) {
            LOGGER.warn("Mirroring of the {} datasource was requested, but it is not enabled", Datasource.CSAF);
            return completedFuture(null);
        }

        return executorService.submit(() -> {
            try {
                mirrorInternal();
                dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                        "Mirroring of configured CSAF sources completed successfully.");
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                LOGGER.error("An unexpected error occurred mirroring configured CSAF sources", e);
                dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                        "An error occurred mirroring configured CSAF sources, cause being: " + e
                                + ". Check log for details.");
            }
        });
    }

    @Transactional
    void mirrorInternal() throws Throwable, Exception {
        var providers = csafSourceRepository.findEnabledProviders();
        var csafLoader = csafLoaderFactory.create();

        LOGGER.info("Mirroring CSAF Vulnerabilities from {} providers", providers.size());
        final Timer.Sample durationSample = Timer.start();

        discoverProvidersFromAggregators(csafLoader);

        for (CsafSourceEntity provider : providers) {
            mirrorProvider(provider, csafLoader);
        }

        final long durationNanos = durationSample.stop(durationTimer);
        LOGGER.info("Mirroring of CSAF vulnerabilities completed in {}", Duration.ofNanos(durationNanos));
    }

    /**
     * Discovers potentially new providers from all enabled aggregators.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    @Transactional
    protected void discoverProvidersFromAggregators(CsafLoader csafLoader) throws InterruptedException {
        var aggregators = csafSourceRepository.findEnabledAggregators();
        for (CsafSourceEntity aggregator : aggregators) {
            try {
                discoverProvider(aggregator, csafLoader);
            } catch (ExecutionException e) {
                LOGGER.error("Error while discovering providers from aggregator {}", aggregator.getUrl(), e);
            }
        }
    }

    protected void discoverProvider(CsafSourceEntity aggregatorEntity, CsafLoader csafLoader) throws ExecutionException, InterruptedException {
        // Check if this contains any providers that we don't know about yet
        final RetrievedAggregator aggregator;

        if (aggregatorEntity.isDomain()) {
            RetrievedAggregator.fromDomainAsync(aggregatorEntity.getUrl(), csafLoader).get();
        } else {
            RetrievedAggregator.fromUrlAsync(aggregatorEntity.getUrl(), csafLoader).get();
        }

        var begin = Instant.now();

        aggregator.fetchAllAsync().get().forEach((provider) -> {
            if (provider.getOrNull() != null) {
                var metadataJson = provider.getOrNull().getJson();
                var url = metadataJson.getCanonical_url().toString();
                var existing = csafSourceRepository.find("url", url);
                // Check if we already know about this provider
                if (existing.count() == 0) {
                    // If not, add it to the list of providers to mirror
                    var newProvider = createCsafSourceEntity(url, metadataJson);
                    csafSourceRepository.persist(newProvider);

                    LOGGER.info("Discovered new CSAF provider {} from aggregator {}", url, aggregatorEntity.getName());
                }
            }
        });

        aggregatorEntity.setLastFetched(begin);
    }

    private static @NotNull CsafSourceEntity createCsafSourceEntity(String url, Provider metadataJson) {
        var newProvider = new CsafSourceEntity();
        newProvider.setUrl(url);
        // We can take the name of the publisher as the name of the provider.
        // The user can change this later
        newProvider.setName(metadataJson.getPublisher().getName());
        // We don't enable it yet
        newProvider.setEnabled(false);
        // It's a provider, not an aggregator
        newProvider.setAggregator(false);
        // Set it to discovery mode so we can inform the user,
        // and he can decide, whether he wants to "add" it to
        // the list of providers to mirror
        newProvider.setDiscovery(true);
        return newProvider;
    }

    /**
     * Mirrors a single provider based on the given URL.
     *
     * @param providerEntity the provider to mirror as a database entity (see {@link CsafSourceEntity})
     * @param csafLoader the CSAF loader to use for fetching documents
     */
    @Transactional
    protected void mirrorProvider(CsafSourceEntity providerEntity, CsafLoader csafLoader) throws InterruptedException, ExecutionException {
        LOGGER.info("Mirroring documents from CSAF provider {} that were modified since {}", providerEntity.getUrl(), providerEntity.getLastFetched());

        final var since = providerEntity.getLastFetched() != null ?
                kotlinx.datetime.Instant.Companion.fromEpochMilliseconds(providerEntity.getLastFetched().toEpochMilli()) : null;
        final var begin = Instant.now();
        final RetrievedProvider provider;

        if (providerEntity.isDomain()) {
            provider = RetrievedProvider.fromDomainAsync(providerEntity.getUrl(), csafLoader).get();
        } else {
            provider = RetrievedProvider.fromUrlAsync(providerEntity.getUrl(), csafLoader).get();
        }

        final var documentStream = provider.streamDocuments(since);
        documentStream.forEach((document) -> {
            if (document.getOrNull() != null) {
                var csaf = document.getOrNull().getJson();
                var raw = Json.Default.encodeToString(Csaf.Companion.serializer(), csaf);

                try {
                    // Build a new CSAF document item to send back to the API server
                    var doc = CsafDocumentItem.newBuilder()
                            .setUrl(providerEntity.getUrl())
                            .setJsonContent(ByteString.copyFromUtf8(raw))
                            .setLastFetched(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                            .setSeen(false)
                            .setName(csaf.getDocument().getTitle())
                            .setPublisherNamespace(csaf.getDocument().getPublisher().getNamespace().toString())
                            .setTrackingId(csaf.getDocument().getTracking().getId())
                            .setTrackingVersion(csaf.getDocument().getTracking().getVersion())
                            .build();

                    LOGGER.info("Processing CSAF document {} from provider {}", csaf.getDocument().getTracking().getId(), providerEntity.getUrl());
                    publishCsafDocument(doc);

                    var vulns = csaf.getVulnerabilities();
                    for (int idx = 0; vulns != null && idx < vulns.size(); idx++) {
                        var vuln = vulns.get(idx);
                            LOGGER.info("Processing vulnerability {}{}", computeVulnerabilityId(vuln, csaf.getDocument(), idx), vuln.getTitle() != null ? " (" + vuln.getTitle() + ")" : "");
                            final Bom bov = CsafToCdxParser.parse(vuln, csaf.getDocument(), idx);
                            publishIfChanged(bov);
                    }
                } catch (ExecutionException | NoSuchAlgorithmException e) {
                    LOGGER.error("Error while processing document", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                LOGGER.error("Error while processing document", document.exceptionOrNull());
            }
        });

        // Update the last fetched date of the provider to the beginning of our update process. This way we can
        // ensure that we don't miss any documents that would be published while we are mirroring (however unlikely).
        providerEntity.setLastFetched(begin);

        LOGGER.info("Mirroring documents from CSAF provider {} completed", providerEntity.getUrl());
    }

    /**
     * Publish CSAF documents to Kafka if they are new or have changed.
     *
     * @param item the item (as a {@link CsafDocumentItem} to publish
     */
    private void publishCsafDocument(CsafDocumentItem item) throws ExecutionException, InterruptedException {
            kafkaProducer.send(new ProducerRecord<>(
                    KafkaTopic.NEW_CSAF_DOCUMENT.getName(), computeKafkaKey(item), item.toByteArray())).get();
    }

    String computeKafkaKey(CsafDocumentItem item) {
        return "CSAF-" + item.getPublisherNamespace() + "-" + item.getTrackingId();
    }

}
