package org.hyades.vulnmirror.datasource.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.NvdCveApiBuilder;

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

    NvdCveApi createApiClient(final long lastModifiedEpochSeconds) {
        final NvdCveApiBuilder builder = NvdCveApiBuilder.aNvdCveApi();

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
