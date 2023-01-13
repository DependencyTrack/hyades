package org.acme.nvd;

import alpine.Config;
import org.acme.client.NvdClient;
import org.acme.model.NvdResourceType;
import org.cyclonedx.model.Bom;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.acme.util.FileUtil.setOutputDir;

@ApplicationScoped
public class NvdMirrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirrorHandler.class);
    private NvdClient client;
    private File outputDir;
    private boolean mirroredWithoutErrors;
    private String cveJsonModifiedUrl;
    private String cveJsonModifiedMeta;
    private String cveJsonBaseUrl;
    private String cveJsonBaseMeta;
    private Integer startYear;
    List<Bom> nvdVulnerabilities;
    private static final int END_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    public static final String NVD_MIRROR_DIR = Config.getInstance().getDataDirectorty().getAbsolutePath() + File.separator + "nist";

    @Inject
    public NvdMirrorHandler(final NvdClient client,
                            @ConfigProperty(name = "mirror.nvd.cve.json11.modified.url") final Optional<String> cveJsonModifiedUrl,
                            @ConfigProperty(name = "mirror.nvd.cve.json11.modified.meta") final Optional<String> cveJsonModifiedMeta,
                            @ConfigProperty(name = "mirror.nvd.cve.json11.base.url") final Optional<String> cveJsonBaseUrl,
                            @ConfigProperty(name = "mirror.nvd.cve.json11.base.meta") final Optional<String> cveJsonBaseMeta,
                            @ConfigProperty(name = "mirror.nvd.start.year") final Integer startYear) {
        this.client = client;
        this.cveJsonModifiedUrl = cveJsonModifiedUrl.orElse(null);
        this.cveJsonModifiedMeta = cveJsonModifiedMeta.orElse(null);
        this.cveJsonBaseUrl = cveJsonBaseUrl.orElse(null);
        this.cveJsonBaseMeta = cveJsonBaseMeta.orElse(null);
        this.startYear = startYear;
        nvdVulnerabilities = new ArrayList<>();
    }

    public List<Bom> performMirror() throws IOException {
        final long start = System.currentTimeMillis();
        LOGGER.info("Starting NIST mirroring task");
        final File mirrorPath = new File(NVD_MIRROR_DIR);
        outputDir = setOutputDir(mirrorPath.getAbsolutePath());
        getAllFiles();
        final long end = System.currentTimeMillis();
        LOGGER.info("NIST mirroring complete");
        LOGGER.info("Time spent (total): " + (end - start) + "ms");
        return nvdVulnerabilities;
    }

    /**
     * Download all NVD XML and JSON feeds from NIST.
     */
    private void getAllFiles() {
        final Date currentDate = new Date();
        LOGGER.info("Downloading files at " + currentDate);
        nvdVulnerabilities.addAll(this.client.downloadNvdFeed(outputDir, this.cveJsonModifiedUrl, NvdResourceType.CVE_MODIFIED_DATA));
        nvdVulnerabilities.addAll(this.client.downloadNvdFeed(outputDir, this.cveJsonModifiedMeta, NvdResourceType.CVE_META));
        for (int i = END_YEAR; i >= this.startYear; i--) {
            // Download JSON 1.1 year feeds in reverse order
            final String json11BaseUrl = this.cveJsonBaseUrl.replace("%d", String.valueOf(i));
            final String cve11BaseMetaUrl = this.cveJsonBaseMeta.replace("%d", String.valueOf(i));
            nvdVulnerabilities.addAll(this.client.downloadNvdFeed(outputDir, json11BaseUrl, NvdResourceType.CVE_YEAR_DATA));
            nvdVulnerabilities.addAll(this.client.downloadNvdFeed(outputDir, cve11BaseMetaUrl, NvdResourceType.CVE_META));
        }
    }
}
