/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.kstreams.exception;

import org.dependencytrack.kstreams.exception.ExceptionHandlerConfig.ThresholdConfig;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

abstract class AbstractThresholdBasedExceptionHandler {

    private final Clock clock;
    private final Duration exceptionThresholdInterval;
    private final int exceptionThresholdCount;
    private Instant firstExceptionOccurredAt;
    private int exceptionOccurrences;

    AbstractThresholdBasedExceptionHandler(final ThresholdConfig config) {
        this.clock = Clock.systemUTC();
        if (config != null) {
            this.exceptionThresholdInterval = config.interval();
            this.exceptionThresholdCount = config.count();
        } else {
            this.exceptionThresholdInterval = Duration.ofMinutes(30);
            this.exceptionThresholdCount = 5;
        }
    }

    AbstractThresholdBasedExceptionHandler(final Clock clock, final Duration exceptionThresholdInterval, final int exceptionThresholdCount) {
        this.clock = clock;
        this.exceptionThresholdInterval = exceptionThresholdInterval;
        this.exceptionThresholdCount = exceptionThresholdCount;
    }

    boolean exceedsThreshold() {
        final Instant now = Instant.now(clock);
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
