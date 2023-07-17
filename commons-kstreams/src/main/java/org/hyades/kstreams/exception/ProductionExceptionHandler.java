package org.hyades.kstreams.exception;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.config.SmallRyeConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

@RegisterForReflection
public class ProductionExceptionHandler implements org.apache.kafka.streams.errors.ProductionExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionExceptionHandler.class);

    private final Duration exceptionThresholdInterval;
    private final int exceptionThresholdCount;
    private Instant firstExceptionOccurredAt;
    private int exceptionOccurrences;

    @SuppressWarnings("unused") // Called by Kafka Streams via reflection
    public ProductionExceptionHandler() {
        ExceptionHandlerConfig exceptionHandlerConfig;
        try {
            final var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
            exceptionHandlerConfig = config.getConfigMapping(ExceptionHandlerConfig.class);
        } catch (NoSuchElementException | IllegalStateException e) {
            // When running tests without @QuarkusTest, resolving of the ConfigMapping will not work.
            LOGGER.debug("State store config could not be resolved; Falling back to conservative defaults", e);
            exceptionHandlerConfig = null;
        }
        if (exceptionHandlerConfig != null) {
            this.exceptionThresholdInterval = exceptionHandlerConfig.thresholds().production().interval();
            this.exceptionThresholdCount = exceptionHandlerConfig.thresholds().production().count();
        } else {
            this.exceptionThresholdInterval = Duration.ofMinutes(30);
            this.exceptionThresholdCount = 5;
        }
    }

    ProductionExceptionHandler(final Duration exceptionThresholdInterval,
                               final int exceptionThresholdCount) {
        this.exceptionThresholdInterval = exceptionThresholdInterval;
        this.exceptionThresholdCount = exceptionThresholdCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Map<String, ?> configs) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ProductionExceptionHandlerResponse handle(final ProducerRecord<byte[], byte[]> record,
                                                                  final Exception exception) {
        if (!(exception instanceof RecordTooLargeException)) {
            LOGGER.error("""
                    Failed to produce record to topic %s; \
                    Stopping to produce records, as the error is of an unexpected type, \
                    and we're not sure if it can safely be ignored\
                    """
                    .formatted(record.topic()), exception);
            return ProductionExceptionHandlerResponse.FAIL;
        }

        final Instant now = Instant.now();
        if (firstExceptionOccurredAt == null) {
            firstExceptionOccurredAt = now;
            exceptionOccurrences = 1;
        } else {
            exceptionOccurrences++;
        }

        final Instant cutoff = firstExceptionOccurredAt.plus(exceptionThresholdInterval);
        if (now.isAfter(cutoff)) {
            firstExceptionOccurredAt = now;
            exceptionOccurrences = 1;
        }

        if (exceptionOccurrences >= exceptionThresholdCount) {
            LOGGER.error("""
                    Failed to produce record to topic %s; \
                    Stopping to produce records, as the error was encountered %d times since %s, \
                    exceeding the configured threshold of %d occurrences in an interval of %s\
                    """
                    .formatted(record.topic(),
                            exceptionOccurrences, firstExceptionOccurredAt,
                            exceptionThresholdCount, exceptionThresholdInterval), exception);
            return ProductionExceptionHandlerResponse.FAIL;
        }

        LOGGER.warn("""
                Failed to produce record to topic %s; \
                Skipping and continuing to produce records, as the configured threshold of \
                %d occurrences in an interval of %s has not been exceeded yet\
                """
                .formatted(record.topic(), exceptionThresholdCount, exceptionThresholdInterval), exception);
        return ProductionExceptionHandlerResponse.CONTINUE;
    }

}
