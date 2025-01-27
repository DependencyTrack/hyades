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
package org.dependencytrack.vulnmirror.datasource.github;

import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.dependencytrack.common.SecretDecryptor;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClientBuilder.aGitHubSecurityAdvisoryClient;

@ApplicationScoped
class GitHubApiClientFactory {

    private final GitHubConfig config;
    private final SecretDecryptor secretDecryptor;

    GitHubApiClientFactory(final GitHubConfig config, final SecretDecryptor secretDecryptor) {
        this.config = config;
        this.secretDecryptor = secretDecryptor;
    }

    GitHubSecurityAdvisoryClient create(final long lastUpdatedEpochSeconds) {
        final HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom()
                .setRetryStrategy(new GitHubHttpRequestRetryStrategy())
                .useSystemProperties();

        final GitHubSecurityAdvisoryClientBuilder builder = aGitHubSecurityAdvisoryClient()
                .withHttpClientSupplier(httpClientBuilder::build);

        config.baseUrl().ifPresent(builder::withEndpoint);
        config.apiKey()
                // TODO: https://github.com/DependencyTrack/dependency-track/issues/3332
//                .map(encryptedApiKey -> {
//                    try {
//                        return secretDecryptor.decryptAsString(encryptedApiKey);
//                    } catch (Exception e) {
//                        throw new IllegalStateException("Failed to decrypt API key", e);
//                    }
//                })
                .ifPresent(builder::withApiKey);

        if (lastUpdatedEpochSeconds > 0) {
            final ZonedDateTime lastUpdated = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastUpdatedEpochSeconds), ZoneOffset.UTC);
            builder.withUpdatedSinceFilter(lastUpdated);
        }

        return builder.build();
    }

}
