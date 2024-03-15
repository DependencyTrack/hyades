package org.dependencytrack.vulnmirror.datasource.epss;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of {@code open-vulnerability-clients} for the EPSS data source for reflection.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
                io.github.jeremylong.openvulnerability.client.epss.EpssDataFeed.class,
                io.github.jeremylong.openvulnerability.client.epss.EpssException.class,
                io.github.jeremylong.openvulnerability.client.epss.EpssItem.class,
                io.github.jeremylong.openvulnerability.client.epss.EpssResponseHandler.class
        },
        ignoreNested = false
)
class EpssReflectionConfiguration {
}
