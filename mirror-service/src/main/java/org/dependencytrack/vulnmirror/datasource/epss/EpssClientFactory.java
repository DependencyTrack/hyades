package org.dependencytrack.vulnmirror.datasource.epss;

import io.github.jeremylong.openvulnerability.client.epss.EpssDataFeed;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class EpssClientFactory {

    private final EpssConfig epssConfig;

    EpssClientFactory(final EpssConfig epssConfig) {
        this.epssConfig = epssConfig;
    }

    EpssDataFeed create() {
        if (epssConfig.downloadUrl().isPresent()) {
            return new EpssDataFeed(epssConfig.downloadUrl().get());
        }
        else {
            // using default location "https://epss.cyentia.com/epss_scores-current.csv.gz"
            return new EpssDataFeed();
        }
    }
}
