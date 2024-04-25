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
package org.dependencytrack.vulnmirror.datasource.nvd;

import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClient;
import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.dependencytrack.common.SecretDecryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@ApplicationScoped
class NvdApiClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdApiClientFactory.class);

    private final NvdConfig config;
    private final SecretDecryptor secretDecryptor;

    NvdApiClientFactory(final NvdConfig config, final SecretDecryptor secretDecryptor) {
        this.config = config;
        this.secretDecryptor = secretDecryptor;
    }

    NvdCveClient createApiClient(final long lastModifiedEpochSeconds) {
        final NvdCveClientBuilder builder = NvdCveClientBuilder.aNvdCveApi();

        config.baseUrl().ifPresent(builder::withEndpoint);
        config.apiKey().ifPresent(encryptedApiKey -> {
            try {
                final String decryptedApiKey = secretDecryptor.decryptAsString(encryptedApiKey);
                builder.withApiKey(decryptedApiKey);
            } catch (Exception e) {
                LOGGER.warn("Failed to decrypt API key, proceeding without authentication", e);
            }
        });

        if (lastModifiedEpochSeconds > 0) {
            final var start = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastModifiedEpochSeconds), ZoneOffset.UTC);
            builder.withLastModifiedFilter(start, start.minusDays(-120));
        }

        return builder.build();
    }

}
