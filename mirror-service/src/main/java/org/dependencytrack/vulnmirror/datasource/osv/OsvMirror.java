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
package org.dependencytrack.vulnmirror.datasource.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.producer.Producer;
import org.cyclonedx.proto.v1_4.Bom;
import org.dependencytrack.vulnmirror.datasource.AbstractDatasourceMirror;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.state.MirrorStateStore;
import org.dependencytrack.vulnmirror.state.VulnerabilityDigestStore;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.dependencytrack.proto.notification.v1.Level.LEVEL_ERROR;
import static org.dependencytrack.proto.notification.v1.Level.LEVEL_INFORMATIONAL;

@ApplicationScoped
class OsvMirror extends AbstractDatasourceMirror<Void> {

    private static final String NOTIFICATION_TITLE = "OSV Mirroring";
    private static final Logger LOGGER = LoggerFactory.getLogger(OsvMirror.class);
    private final ExecutorService executorService;
    private final OsvClient client;
    private final ObjectMapper objectMapper;
    private final OsvConfig osvConfig;

    OsvMirror(@ForOsvMirror ExecutorService executorService, OsvClient client,
              @ForOsvMirror ObjectMapper objectMapper, final MirrorStateStore mirrorStateStore,
              final VulnerabilityDigestStore vulnDigestStore,
              final Producer<String, byte[]> bovProducer,
              final OsvConfig osvConfig) {
        super(Datasource.OSV, mirrorStateStore, vulnDigestStore, bovProducer, Void.class);
        this.executorService = executorService;
        this.client = client;
        this.objectMapper = objectMapper;
        this.osvConfig = osvConfig;
    }

    void performMirror(String ecosystem) throws IOException, ExecutionException, InterruptedException {
        Path ecosystemZip = client.downloadEcosystemZip(ecosystem);
        try (InputStream inputStream = Files.newInputStream(ecosystemZip, StandardOpenOption.DELETE_ON_CLOSE);
             ZipInputStream zipInput = new ZipInputStream(inputStream)) {
            parseZipInputAndPublishIfChanged(zipInput);
        }

    }

    private void parseZipInputAndPublishIfChanged(ZipInputStream zipIn) throws IOException, ExecutionException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn, StandardCharsets.UTF_8));
        ZipEntry zipEntry = zipIn.getNextEntry();
        while (zipEntry != null) {

            String line = null;
            StringBuilder out = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            var json = new JSONObject(out.toString());
            Bom bov = new OsvToCyclonedxParser(this.objectMapper).parse(json, this.osvConfig.aliasSyncEnabled().orElse(false));
            if (bov != null) {
                publishIfChanged(bov);
            }
            zipEntry = zipIn.getNextEntry();
            reader = new BufferedReader(new InputStreamReader(zipIn));
        }
        reader.close();

    }

    @Override
    public Future<?> doMirror() {
        if (osvConfig.enabledEcosystems().isEmpty()) {
            LOGGER.warn("Mirroring of the {} datasource was requested, but no ecosystem is enabled for it", Datasource.OSV);
            return CompletableFuture.completedFuture(null);
        }

        final var ecosystemFutures = new ArrayList<CompletableFuture<?>>();
        for (final String ecosystem : osvConfig.enabledEcosystems()) {
            ecosystemFutures.add(CompletableFuture.runAsync(() -> doMirrorEcosystem(ecosystem), executorService));
        }

        return CompletableFuture.allOf(ecosystemFutures.toArray(new CompletableFuture[0]));
    }

    private void doMirrorEcosystem(final String ecosystem) {
        try {
            performMirror(ecosystem);
            dispatchNotification(LEVEL_INFORMATIONAL, NOTIFICATION_TITLE,
                    "OSV mirroring completed for ecosystem: " + ecosystem);
        } catch (InterruptedException e) {
            LOGGER.warn("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred mirroring the contents of ecosystem: {}", ecosystem, e);
            dispatchNotification(LEVEL_ERROR, NOTIFICATION_TITLE,
                    "An error occurred mirroring the contents of ecosystem :" + ecosystem + " for OSV. Check log for details.");
        }
    }

}
