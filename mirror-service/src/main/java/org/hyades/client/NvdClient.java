package org.hyades.client;

import io.github.jeremylong.nvdlib.NvdCveApi;
import io.github.jeremylong.nvdlib.nvd.DefCveItem;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.cyclonedx.model.Bom;
import org.hyades.model.Vulnerability;
import org.hyades.nvd.NvdMirrorHandler;
import org.hyades.nvd.NvdToCyclonedxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Client for the NVD REST API.
 */
public class NvdClient extends ContextualProcessor<String, String, String, Bom>
        implements ProcessorSupplier<String, String, String, Bom> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdClient.class);
    private static final String LAST_MODIFIED_EPOCH_KEY = "LAST_MODIFIED_EPOCH_KEY";
    private KeyValueStore<String, Long> store;
    private StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder;
    private final BiFunction<String, Long, NvdCveApi> cveApiSupplier;
    private String apiKey;

    Collection<DefCveItem> nvdFeeds;

    public NvdClient(StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder,
                     String apiKey, BiFunction<String, Long, NvdCveApi> cveApiSupplier) {
        this.apiKey = apiKey;
        this.nvdFeeds = new ArrayList<>();
        this.lastModifiedEpochStoreBuilder = lastModifiedEpochStoreBuilder;
        this.cveApiSupplier = cveApiSupplier;
    }

    @Override
    public void init(ProcessorContext context) {
        super.init(context);
        this.store = context().getStateStore(this.lastModifiedEpochStoreBuilder.name());
    }

    long retrieveLastModifiedRequestEpoch() {
        if (this.store != null && store.get(LAST_MODIFIED_EPOCH_KEY) != null) {
            return store.get(LAST_MODIFIED_EPOCH_KEY);
        }
        return 0;
    }

    void storeLastModifiedRequestEpoch(long epoch) {
        if (this.store != null) {
            this.store.put(LAST_MODIFIED_EPOCH_KEY, epoch);
        }
    }

    public void update(Record record) {
        long lastModifiedRequest = retrieveLastModifiedRequestEpoch();
        try (NvdCveApi api = this.cveApiSupplier.apply(apiKey, lastModifiedRequest)) {
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
        return new NvdClient(this.lastModifiedEpochStoreBuilder, this.apiKey, this.cveApiSupplier);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(this.lastModifiedEpochStoreBuilder);
    }
}
