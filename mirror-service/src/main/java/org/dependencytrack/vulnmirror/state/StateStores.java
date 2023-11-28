package org.dependencytrack.vulnmirror.state;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;

/**
 * Utilities for accessing Kafka Streams state stores for interactive queries.
 *
 * @see <a href="https://kafka.apache.org/34/documentation/streams/developer-guide/interactive-queries.html">Interactive Queries</a>
 */
public final class StateStores {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateStores.class);

    public static final String MIRROR_STATES = "mirror-states";
    public static final String VULNERABILITY_DIGESTS = "vulnerability-digests";

    private StateStores() {
    }

    /**
     * Get a {@link ReadOnlyKeyValueStore} with a given name.
     * <p>
     * Will wait up to 5 seconds for the state store to become accessible,
     * in case it is not ready yet, for example during a re-balance.
     *
     * @param name Name of the key-value store to get
     * @param <K>  Type of the key
     * @param <V>  Type of the value
     * @return The key-value state store
     * @throws IllegalStateException      When no {@link KafkaStreams} instance could be discovered via CDI
     * @throws InvalidStateStoreException When the state store could not be accessed
     */
    static <K, V> ReadOnlyKeyValueStore<K, V> keyValueStore(final String name) {
        final Instance<KafkaStreams> kafkaStreamsInstance = CDI.current().select(KafkaStreams.class);
        if (!kafkaStreamsInstance.isResolvable()) {
            throw new IllegalStateException("Unable to resolve KafkaStreams bean from CDI context");
        }

        final KafkaStreams kafkaStreams = kafkaStreamsInstance.get();

        return Failsafe
                .with(RetryPolicy.builder()
                        .handle(InvalidStateStoreException.class)
                        .onRetry(event -> LOGGER.debug("State store {} is not ready yet; Retrying", name, event.getLastException()))
                        .onRetriesExceeded(event -> LOGGER.warn("Max retries exceeded while waiting for state store {} to become ready", name, event.getException()))
                        .onSuccess(event -> LOGGER.debug("State store {} is ready", name))
                        .withDelay(Duration.ofMillis(50))
                        .withMaxRetries(100)
                        .build())
                .get(() -> kafkaStreams.store(fromNameAndType(name, QueryableStoreTypes.keyValueStore())));
    }

}
