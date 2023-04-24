package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClient;
import io.github.jeremylong.openvulnerability.client.nvd.NvdCveClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@ApplicationScoped
class NvdApiClientFactory {

    private final NvdConfig config;

    NvdApiClientFactory(final NvdConfig config) {
        this.config = config;
    }

    NvdCveClient createApiClient(final long lastModifiedEpochSeconds) {
        final NvdCveClientBuilder builder = NvdCveClientBuilder.aNvdCveApi();

        config.baseUrl().ifPresent(builder::withEndpoint);
        config.apiKey().ifPresent(apiKey -> {
            builder.withApiKey(apiKey);
            builder.withThreadCount(config.numThreads());
        });

        if (lastModifiedEpochSeconds > 0) {
            final var start = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastModifiedEpochSeconds), ZoneOffset.UTC);
            builder.withLastModifiedFilter(start, start.minusDays(-120));
        }

        return builder.build();
    }

}
