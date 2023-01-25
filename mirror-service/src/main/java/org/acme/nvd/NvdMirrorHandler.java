package org.acme.nvd;

import org.acme.client.NvdClient;
import org.apache.kafka.streams.processor.api.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class NvdMirrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdMirrorHandler.class);
    private NvdClient client;

    @Inject
    public NvdMirrorHandler(NvdClient client) {
        this.client = client;
    }

    public void performMirror(Record record) {
        long start = System.currentTimeMillis();
        LOGGER.info("Starting NVD mirroring task");
        this.client.update(record);
        long end = System.currentTimeMillis();
        LOGGER.info("NVD mirroring complete. Time spent (total): " + (end - start) + "ms");
    }
}
