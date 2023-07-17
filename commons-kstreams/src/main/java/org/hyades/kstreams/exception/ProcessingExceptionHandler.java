package org.hyades.kstreams.exception;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD;
import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_CLIENT;

@ApplicationScoped
public class ProcessingExceptionHandler implements StreamsUncaughtExceptionHandler {

    private record ExceptionOccurrence(Instant occurredFirstAt, int count) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingExceptionHandler.class);
    private static final Set<String> DEFAULT_TRANSIENT_EXCEPTION_NAMES = Set.of(
            "java.net.SocketTimeoutException",
            "java.sql.SQLTransientException",
            "java.util.concurrent.TimeoutException",
            "org.apache.http.conn.ConnectTimeoutException",
            "org.hibernate.QueryTimeoutException"
    );

    private final Map<Class<? extends Throwable>, ExceptionOccurrence> transientExceptionOccurrences;
    private final Duration transientExceptionThresholdInterval;
    private final int transientExceptionThresholdCount;


    @SuppressWarnings("unused")
    ProcessingExceptionHandler(final ExceptionHandlerConfig config) {
        this.transientExceptionOccurrences = new ConcurrentHashMap<>();
        this.transientExceptionThresholdInterval = config.thresholds().processing().interval();
        this.transientExceptionThresholdCount = config.thresholds().processing().count();
    }

    ProcessingExceptionHandler(final Duration transientExceptionThresholdInterval,
                               final int transientExceptionThresholdCount) {
        this.transientExceptionOccurrences = new ConcurrentHashMap<>();
        this.transientExceptionThresholdInterval = transientExceptionThresholdInterval;
        this.transientExceptionThresholdCount = transientExceptionThresholdCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamThreadExceptionResponse handle(final Throwable throwable) {
        final Throwable rootCause = ExceptionUtils.getRootCause(throwable);

        if (isTransient(rootCause)) {
            final ExceptionOccurrence occurrence = transientExceptionOccurrences
                    .compute(rootCause.getClass(), (key, oldValue) -> {
                        final Instant now = Instant.now();
                        if (oldValue == null) {
                            return new ExceptionOccurrence(now, 1);
                        }

                        final Instant cutoff = oldValue.occurredFirstAt().plus(transientExceptionThresholdInterval);
                        if (now.isAfter(cutoff)) {
                            return new ExceptionOccurrence(now, 1);
                        }

                        return new ExceptionOccurrence(oldValue.occurredFirstAt(), oldValue.count() + 1);
                    });

            if (occurrence.count() >= transientExceptionThresholdCount) {
                LOGGER.error("""
                        Encountered an unhandled exception during record processing; \
                        Shutting down the failed stream thread as the error was encountered \
                        %d times since %s, exceeding the configured threshold of %d occurrences \
                        in an interval of %s\
                        """
                        // Actual exception stack trace will be logged by Kafka Streams
                        .formatted(occurrence.count(), occurrence.occurredFirstAt(),
                                transientExceptionThresholdCount, transientExceptionThresholdInterval));
                return SHUTDOWN_CLIENT;
            }

            LOGGER.warn("""
                    Encountered an unhandled exception during record processing; \
                    Replacing the failed stream thread as the error appears to be transient\
                    """); // Actual exception stack trace will be logged by Kafka Streams
            return REPLACE_THREAD;
        }

        LOGGER.error("""
                Encountered an unhandled exception during record processing; \
                Shutting down the failed stream thread as the error does not appear to be transient\
                """); // Actual exception stack trace will be logged by Kafka Streams
        return SHUTDOWN_CLIENT;
    }

    protected boolean isTransient(final Throwable rootCause) {
        return DEFAULT_TRANSIENT_EXCEPTION_NAMES.contains(rootCause.getClass().getName());
    }

}
