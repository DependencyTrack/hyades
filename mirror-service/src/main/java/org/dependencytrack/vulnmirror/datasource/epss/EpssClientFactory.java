package org.dependencytrack.vulnmirror.datasource.epss;

import io.github.jeremylong.openvulnerability.client.epss.EpssDataFeed;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class EpssClientFactory {

    EpssDataFeed create(final String downloadUrl) {

        if (downloadUrl == null) {
            // using default location "https://epss.cyentia.com/epss_scores-current.csv.gz"
            return new EpssDataFeed();
        }
        return new EpssDataFeed(downloadUrl);
    }

}
