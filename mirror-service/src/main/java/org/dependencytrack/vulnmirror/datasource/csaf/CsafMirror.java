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

import io.github.csaf.sbom.retrieval.CsafLoader;
import io.github.csaf.sbom.retrieval.RetrievedAggregator;
import io.github.csaf.sbom.retrieval.RetrievedProvider;
import io.github.csaf.sbom.schema.generated.Csaf;
import io.github.csaf.sbom.schema.generated.Provider;
import io.micrometer.core.instrument.Timer;
import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import kotlinx.serialization.json.Json;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_6.Bom;
import org.dependencytrack.persistence.model.CsafDocumentEntity;
import org.dependencytrack.persistence.model.CsafSourceEntity;
import org.dependencytrack.persistence.repository.CsafDocumentRepository;
import org.dependencytrack.persistence.repository.CsafSourceRepository;
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
import static org.dependencytrack.vulnmirror.datasource.csaf.CsafToCdxParser.computeDocumentId;
import static org.dependencytrack.vulnmirror.datasource.csaf.CsafToCdxParser.computeVulnerabilityId;

@ApplicationScoped
public class CsafMirror extends AbstractDatasourceMirror<CsafMirrorState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsafMirror.class);
    private static final String NOTIFICATION_TITLE = "CSAF Mirroring";

    private final CsafConfig config;
    private final CsafLoader csafLoader;
    private final ExecutorService executorService;
    private final CsafSourceRepository csafSourceRepository;
    private final CsafDocumentRepository csafDocumentRepository;
    private final Timer durationTimer;

    CsafMirror(
            final CsafConfig config,
            @ForCsafMirror final ExecutorService executorService,
            final CsafSourceRepository csafSourceRepository,
            final CsafDocumentRepository csafDocumentRepository,
            final MirrorStateStore mirrorStateStore,
            final VulnerabilityDigestStore vulnDigestStore,
            final Producer<String, byte[]> kafkaProducer,
            @ForCsafMirror final CsafLoader csafLoader,
            @ForCsafMirror final Timer durationTimer) {
        super(Datasource.CSAF, mirrorStateStore, vulnDigestStore, kafkaProducer, CsafMirrorState.class);
        this.config = config;
        this.executorService = executorService;
        this.csafSourceRepository = csafSourceRepository;
        this.csafDocumentRepository = csafDocumentRepository;
        this.csafLoader = csafLoader;
        this.durationTimer = durationTimer;
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

        LOGGER.info("Mirroring CSAF Vulnerabilities from {} providers", providers.size());
        final Timer.Sample durationSample = Timer.start();

        discoverProvidersFromAggregators();

        for (CsafSourceEntity provider : providers) {
            mirrorProvider(provider);
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
    protected void discoverProvidersFromAggregators() throws InterruptedException {
        var aggregators = csafSourceRepository.findEnabledAggregators();
        for (CsafSourceEntity aggregator : aggregators) {
            try {
                discoverProvider(aggregator);
            } catch (ExecutionException e) {
                LOGGER.error("Error while discovering providers from aggregator {}", aggregator.getUrl(), e);
            }
        }
    }

    protected void discoverProvider(CsafSourceEntity aggregatorEntity) throws ExecutionException, InterruptedException {
        // Check if this contains any providers that we don't know about yet
        var aggregator = RetrievedAggregator.fromAsync(aggregatorEntity.getUrl()).get();
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
     */
    @Transactional
    protected void mirrorProvider(CsafSourceEntity providerEntity) throws InterruptedException, ExecutionException {
        LOGGER.info("Mirroring documents from CSAF provider {} that were modified since {}", providerEntity.getUrl(), providerEntity.getLastFetched());

        final var since = providerEntity.getLastFetched() != null ?
                kotlinx.datetime.Instant.Companion.fromEpochMilliseconds(providerEntity.getLastFetched().toEpochMilli()) : null;
        final var begin = Instant.now();
        final var provider = RetrievedProvider.fromUrlAsync(providerEntity.getUrl()).get();
        final var documentStream = provider.streamDocuments(since);
        documentStream.forEach((document) -> {
            if (document.isSuccess()) {
                var csaf = document.getOrNull().getJson();

                // TODO: Can we access the raw directly?
                var raw = Json.Default.encodeToString(Csaf.Companion.serializer(), csaf);

                // Build a new CSAF entity in our database
                var csafEntity = new CsafDocumentEntity();
                try {
                    csafEntity.setId(computeDocumentId(csaf.getDocument()));
                    csafEntity.setUrl(providerEntity.getUrl());
                    csafEntity.setContent(raw);
                    csafEntity.setFetchInterval(0);
                    csafEntity.setSeen(false);
                    csafEntity.setName(csaf.getDocument().getTitle());

                    Panache.getEntityManager().merge(csafEntity);

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

}
