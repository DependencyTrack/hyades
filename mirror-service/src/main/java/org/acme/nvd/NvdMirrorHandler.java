package org.acme.nvd;

import alpine.Config;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import org.acme.client.NvdClient;
import org.cyclonedx.model.Bom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.acme.util.FileUtil.setOutputDir;

@ApplicationScoped
public class NvdMirrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirrorHandler.class);
    private NvdClient client;
    private File outputDir;
    List<Bom> nvdVulnerabilities;
    public static final String NVD_MIRROR_DIR = Config.getInstance().getDataDirectorty().getAbsolutePath() + File.separator + "nist";

    @Inject
    public NvdMirrorHandler(final NvdClient client) {
        this.client = client;
        nvdVulnerabilities = new ArrayList<>();
    }

    public List<Bom> performMirror() {
        final long start = System.currentTimeMillis();
        LOGGER.info("Starting NVD mirroring task");
        final File mirrorPath = new File(NVD_MIRROR_DIR);
        outputDir = setOutputDir(mirrorPath.getAbsolutePath());
        getNvdFeeds();
        final long end = System.currentTimeMillis();
        LOGGER.info("NVD mirroring complete");
        LOGGER.info("Time spent (total): " + (end - start) + "ms");
        return nvdVulnerabilities;
    }

    /**
     * Download all NVD feeds from NIST and parse them to CDX.
     */
    private void getNvdFeeds() {
        Collection<DefCveItem> nvdFeeds = this.client.update();
        final NvdToCyclonedxParser parser = new NvdToCyclonedxParser();
        nvdFeeds.stream().forEach(nvdFeed -> nvdVulnerabilities.add(parser.parse(nvdFeed.getCve())));
    }
}
