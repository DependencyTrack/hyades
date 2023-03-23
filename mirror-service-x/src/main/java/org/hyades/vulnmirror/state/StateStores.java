package org.hyades.vulnmirror.state;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.time.Duration;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;

public final class StateStores {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateStores.class);

    public static final String MIRROR_STATES = "mirror-states";
    public static final String VULNERABILITY_DIGESTS = "vulnerability-digests";

    private StateStores() {
    }

    static <K, V> ReadOnlyKeyValueStore<K, V> keyValueStore(final String name) {
        final Instance<KafkaStreams> kafkaStreamsInstance = CDI.current().select(KafkaStreams.class);
        if (!kafkaStreamsInstance.isResolvable()) {
            throw new IllegalStateException("Unable to resolve KafkaStreams bean from CDI context");
        }

        final KafkaStreams kafkaStreams = kafkaStreamsInstance.get();

        return Failsafe
                .with(RetryPolicy.builder()
                        .handle(InvalidStateStoreException.class)
                        .onRetry(event -> LOGGER.debug("State store {} is not ready yet; Retrying", name))
                        .onRetriesExceeded(event -> LOGGER.debug("Retries exceeded for state store {}", name))
                        .onSuccess(event -> LOGGER.debug("State store {} is ready", name))
                        .withDelay(Duration.ofMillis(50))
                        .withMaxRetries(100)
                        .build())
                .get(() -> kafkaStreams.store(fromNameAndType(name, QueryableStoreTypes.keyValueStore())));
    }

}
