package org.acme.client;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.NvdCveApiBuilder;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import org.acme.model.Vulnerability;
import org.acme.nvd.NvdMirrorHandler;
import org.acme.nvd.NvdToCyclonedxParser;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.cyclonedx.model.Bom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Client for the NVD REST API.
 */
public class NvdClient extends ContextualProcessor<String, String, String, Bom>
        implements ProcessorSupplier<String, String, String, Bom> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdClient.class);
    private static final String LAST_MODIFIED_EPOCH_KEY = "LAST_MODIFIED_EPOCH_KEY";
    private KeyValueStore<String, Long> store;
    private StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder;
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
        return store.get(LAST_MODIFIED_EPOCH_KEY) != null ? store.get(LAST_MODIFIED_EPOCH_KEY) : 0;
    }

    void storeLastModifiedRequestEpoch(long epoch) {
        if (this.store != null) {
            this.store.put(LAST_MODIFIED_EPOCH_KEY, epoch);
        }
    }

    public void update(Record record) {
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
                List<Bom> bovs = new ArrayList<>();
                api.next().stream().forEach(defCveItem -> {
                    var parser = new NvdToCyclonedxParser();
                    bovs.add(parser.parse(defCveItem.getCve()));
                });
                bovs.forEach(bov -> context().forward(record
                        .withKey(Vulnerability.Source.NVD.name() + "/" + bov.getVulnerabilities().get(0).getId())
                        .withValue(bov)));
            }
            lastModifiedRequest = api.getLastModifiedRequest();
            LOGGER.info("NVD mirroring completed successfully.");
        } catch (Exception e) {
            LOGGER.error("Exception while performing NVD mirroring " + e);
        }
        storeLastModifiedRequestEpoch(lastModifiedRequest);
    }

    @Override
    public void process(Record record) {
        final NvdMirrorHandler nvdMirrorHandler = new NvdMirrorHandler(this);
        nvdMirrorHandler.performMirror(record);
    }

    @Override
    public Processor<String, String, String, Bom> get() {
        return new NvdClient(this.lastModifiedEpochStoreBuilder, this.apiKey);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(this.lastModifiedEpochStoreBuilder);
    }
}
