package org.acme.nvd;

import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import org.acme.client.NvdClient;
import org.cyclonedx.model.Bom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class NvdMirrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirrorHandler.class);
    private NvdClient client;
    List<Bom> nvdVulnerabilities;
    @Inject
    public NvdMirrorHandler(NvdClient client) {
        this.client = client;
        nvdVulnerabilities = new ArrayList<>();
    }

    public List<Bom> performMirror() {
        long start = System.currentTimeMillis();
        LOGGER.info("Starting NVD mirroring task");
        getNvdFeeds();
        long end = System.currentTimeMillis();
        LOGGER.info("NVD mirroring complete");
        LOGGER.info("Time spent (total): " + (end - start) + "ms");
        return nvdVulnerabilities;
    }

    /**
     * Download all NVD feeds from NIST and parse them to CDX.
     */
    private void getNvdFeeds() {
        Collection<DefCveItem> nvdFeeds = this.client.update();
        NvdToCyclonedxParser parser = new NvdToCyclonedxParser();
        nvdFeeds.stream().forEach(nvdFeed -> nvdVulnerabilities.add(parser.parse(nvdFeed.getCve())));
    }
}
