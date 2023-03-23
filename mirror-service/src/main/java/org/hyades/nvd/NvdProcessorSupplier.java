package org.hyades.nvd;

import io.github.jeremylong.nvdlib.NvdCveApi;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.cyclonedx.model.Bom;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;
import java.util.function.BiFunction;

@ApplicationScoped
public class NvdProcessorSupplier implements ProcessorSupplier<String, String, String, Bom> {

    private final String apiKey;
    private final BiFunction<String, Long, NvdCveApi> cveApiSupplier;
    private final StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder;

    @Inject
    public NvdProcessorSupplier(final StoreBuilder<KeyValueStore<String, Long>> lastModifiedEpochStoreBuilder,
                                final NvdConfig config,
                                final BiFunction<String, Long, NvdCveApi> cveApiSupplier) {
        this.apiKey = config.api().apiKey().orElse(null);
        this.lastModifiedEpochStoreBuilder = lastModifiedEpochStoreBuilder;
        this.cveApiSupplier = cveApiSupplier;
    }

    @Override
    public Processor<String, String, String, Bom> get() {
        return new NvdProcessor(this.lastModifiedEpochStoreBuilder, this.apiKey, this.cveApiSupplier);
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(this.lastModifiedEpochStoreBuilder);
    }
}
