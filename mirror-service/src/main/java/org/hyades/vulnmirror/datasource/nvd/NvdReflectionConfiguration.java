package org.hyades.vulnmirror.datasource.nvd;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes of {@code open-vulnerability-clients} for the NVD data source for reflection.
 * <p>
 * The model classes are generated from JSON in {@code nvd-lib}.
 * When inspecting the library's code on GitHub, you won't find these classes.
 */
@SuppressWarnings("unused")
@RegisterForReflection(
        targets = {
                io.github.jeremylong.openvulnerability.client.nvd.Config.class,
                io.github.jeremylong.openvulnerability.client.nvd.CpeMatch.class,
                io.github.jeremylong.openvulnerability.client.nvd.CveApiJson20.class,
                io.github.jeremylong.openvulnerability.client.nvd.CveItem.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV2.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV20.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV30.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV30Data.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV31.class,
                io.github.jeremylong.openvulnerability.client.nvd.CvssV31Data.class,
                io.github.jeremylong.openvulnerability.client.nvd.DefCveItem.class,
                io.github.jeremylong.openvulnerability.client.nvd.LangString.class,
                io.github.jeremylong.openvulnerability.client.nvd.Metrics.class,
                io.github.jeremylong.openvulnerability.client.nvd.Node.class,
                io.github.jeremylong.openvulnerability.client.nvd.Reference.class,
                io.github.jeremylong.openvulnerability.client.nvd.VendorComment.class,
                io.github.jeremylong.openvulnerability.client.nvd.Weakness.class
        },
        ignoreNested = false
)
class NvdReflectionConfiguration {
}
