package org.hyades.kstreams.exception;

import org.hyades.kstreams.exception.ExceptionHandlerConfig.ThresholdConfig;

import java.time.Duration;
import java.time.Instant;

abstract class AbstractThresholdBasedExceptionHandler {

    private final Duration exceptionThresholdInterval;
    private final int exceptionThresholdCount;
    private Instant firstExceptionOccurredAt;
    private int exceptionOccurrences;

    AbstractThresholdBasedExceptionHandler(final ThresholdConfig config) {
        if (config != null) {
            this.exceptionThresholdInterval = config.interval();
            this.exceptionThresholdCount = config.count();
        } else {
            this.exceptionThresholdInterval = Duration.ofMinutes(30);
            this.exceptionThresholdCount = 5;
        }
    }

    AbstractThresholdBasedExceptionHandler(final Duration exceptionThresholdInterval, final int exceptionThresholdCount) {
        this.exceptionThresholdInterval = exceptionThresholdInterval;
        this.exceptionThresholdCount = exceptionThresholdCount;
    }

    boolean exceedsThreshold() {
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

        return exceptionOccurrences >= exceptionThresholdCount;
    }

    public Duration exceptionThresholdInterval() {
        return exceptionThresholdInterval;
    }

    public int exceptionThresholdCount() {
        return exceptionThresholdCount;
    }

    public Instant firstExceptionOccurredAt() {
        return firstExceptionOccurredAt;
    }

    public int exceptionOccurrences() {
        return exceptionOccurrences;
    }

}
