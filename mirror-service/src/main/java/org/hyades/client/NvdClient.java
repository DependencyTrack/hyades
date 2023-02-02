package org.hyades.client;

import io.github.jeremylong.nvdlib.NvdCveApi;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.cyclonedx.model.Bom;
import org.hyades.model.Vulnerability;
import org.hyades.nvd.NvdToCyclonedxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Client for the NVD REST API.
 */
public class NvdClient extends ContextualProcessor<String, String, String, Bom> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdClient.class);
    private static final String LAST_MODIFIED_EPOCH = "LAST_MODIFIED_EPOCH";
    private KeyValueStore<String, Long> store;
    private final StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder;
    private final BiFunction<String, Long, NvdCveApi> cveApiSupplier;
    private final String apiKey;

    public NvdClient(StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder,
                     String apiKey, BiFunction<String, Long, NvdCveApi> cveApiSupplier) {
        this.apiKey = apiKey;
        this.lastModifiedEpochStoreBuilder = lastModifiedEpochStoreBuilder;
        this.cveApiSupplier = cveApiSupplier;
    }

    @Override
    public void init(ProcessorContext<String, Bom> context) {
        super.init(context);
        this.store = context().getStateStore(this.lastModifiedEpochStoreBuilder.name());
    }

    private long retrieveLastModifiedRequestEpoch() {
        if (this.store != null && store.get(LAST_MODIFIED_EPOCH) != null) {
            return store.get(LAST_MODIFIED_EPOCH);
        }
        return 0;
    }

    private void storeLastModifiedRequestEpoch(long epoch) {
        if (this.store != null) {
            this.store.put(LAST_MODIFIED_EPOCH, epoch);
        }
    }

    public void update(Record<String, String> record) {
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
    public void process(Record<String, String> record) {
        long start = System.currentTimeMillis();
        LOGGER.info("Starting NVD mirroring task");
        update(record);
        long end = System.currentTimeMillis();
        LOGGER.info("NVD mirroring complete. Time spent (total): " + (end - start) + "ms");
    }
}
