package org.dependencytrack.vulnmirror.datasource.github;

import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClientBuilder;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClientBuilder.aGitHubSecurityAdvisoryClient;

@ApplicationScoped
class GitHubApiClientFactory {

    private final GitHubConfig config;

    GitHubApiClientFactory(final GitHubConfig config) {
        this.config = config;
    }

    GitHubSecurityAdvisoryClient create(final long lastUpdatedEpochSeconds) {
        final GitHubSecurityAdvisoryClientBuilder builder = aGitHubSecurityAdvisoryClient();

        config.baseUrl().ifPresent(builder::withEndpoint);
        config.apiKey().ifPresent(builder::withApiKey);

        if (lastUpdatedEpochSeconds > 0) {
            final ZonedDateTime lastUpdated = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastUpdatedEpochSeconds), ZoneOffset.UTC);
            builder.withUpdatedSinceFilter(lastUpdated);
        }

        return builder.build();
    }

}
