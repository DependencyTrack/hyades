package org.acme.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.NvdCveApiBuilder;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Client for the NVD REST API.
 */
public class NvdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdClient.class);
    private final ObjectMapper objectMapper;
    private String apiKey;

    Collection<DefCveItem> nvdFeeds;

    public NvdClient(final ObjectMapper objectMapper,
                     final String apiKey) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.nvdFeeds = new ArrayList<>();
    }

    long retrieveLastModifiedRequestEpoch() {
        //TODO implement a storage/retrieval mechanism for the epoch time.
        // if the last modified request epoch is not available the method should return 0
        return 0;
    }

    void storeLastModifiedRequestEpoch(long epoch) {
        //TODO implement a storage/retrieval mechanism for the epoch time.
    }

    public Collection<DefCveItem> update() {
        long lastModifiedRequest = retrieveLastModifiedRequestEpoch();
        NvdCveApiBuilder builder = NvdCveApiBuilder.aNvdCveApi();
        if (lastModifiedRequest > 0) {
            LocalDateTime start = LocalDateTime.ofEpochSecond(lastModifiedRequest, 0, ZoneOffset.UTC);
            LocalDateTime end = start.minusDays(-120);
            builder.withLastModifiedFilter(start, end);
        }
        if (this.apiKey != null) {
            builder.withApiKey(this.apiKey);
        }
        builder.withThreadCount(4);
        builder.withPublishedDateFilter(LocalDateTime.of(LocalDate.of(2002, 1, 1), LocalTime.MIN),
                LocalDateTime.now());
        try (NvdCveApi api = builder.build()) {
            while (api.hasNext()) {
                nvdFeeds.addAll(api.next());
            }
            lastModifiedRequest = api.getLastModifiedRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        storeLastModifiedRequestEpoch(lastModifiedRequest);
        return nvdFeeds;
    }
}
