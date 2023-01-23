package org.acme.client;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.NvdCveApiBuilder;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Client for the NVD REST API.
 */
public class NvdClient extends ContextualProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdClient.class);
    private String lastModifiedEpochKey = "LAST_MODIFIED_EPOCH_KEY";
    private KeyValueStore<String, Long> store;
    StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder;
    private String apiKey;

    Collection<DefCveItem> nvdFeeds;

    public NvdClient(StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder,
                     String apiKey) {
        this.apiKey = apiKey;
        this.nvdFeeds = new ArrayList<>();
        this.lastModifiedEpochStoreBuilder = lastModifiedEpochStoreBuilder;
    }

    @Override
    public void init(ProcessorContext context) {
        super.init(context);
        this.store = context().getStateStore(this.lastModifiedEpochStoreBuilder.name());
    }

    long retrieveLastModifiedRequestEpoch() {
        if(this.store != null) {
            return this.store.get(this.lastModifiedEpochKey);
        }
        return 0;
    }

    void storeLastModifiedRequestEpoch(long epoch) {
        if (this.store != null) {
            this.store.put(this.lastModifiedEpochKey, epoch);
        }
    }

    public Collection<DefCveItem> update() {
        long lastModifiedRequest = retrieveLastModifiedRequestEpoch();
        NvdCveApiBuilder builder = NvdCveApiBuilder.aNvdCveApi();
        if (lastModifiedRequest > 0) {
            var start = LocalDateTime.ofEpochSecond(lastModifiedRequest, 0, ZoneOffset.UTC);
            var end = start.minusDays(-120);
            builder.withLastModifiedFilter(start, end);
        }
        if (this.apiKey != null) {
            builder.withApiKey(this.apiKey);
        }
        builder.withThreadCount(4);
        try (NvdCveApi api = builder.build()) {
            while (api.hasNext()) {
                nvdFeeds.addAll(api.next());
            }
            lastModifiedRequest = api.getLastModifiedRequest();
            LOGGER.info("NVD mirroring completed successfully.");
        } catch (Exception e) {
            throw new WebApplicationException("Exception while performing NVD mirroring", e);
        }
        storeLastModifiedRequestEpoch(lastModifiedRequest);
        return nvdFeeds;
    }

    @Override
    public void process(Record record) {}

    @Override
    public void close() {
        super.close();
    }
}
