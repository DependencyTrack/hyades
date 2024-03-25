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

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.dependencytrack.common.config.QuarkusConfigUtil;
import org.dependencytrack.kstreams.exception.ExceptionHandlerConfig.ThresholdsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;

@RegisterForReflection
public class ProductionExceptionHandler extends AbstractThresholdBasedExceptionHandler
        implements org.apache.kafka.streams.errors.ProductionExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionExceptionHandler.class);

    @SuppressWarnings("unused") // Called by Kafka Streams via reflection
    public ProductionExceptionHandler() {
        super(QuarkusConfigUtil.getConfigMapping(ExceptionHandlerConfig.class)
                .map(ExceptionHandlerConfig::thresholds)
                .map(ThresholdsConfig::production)
                .orElse(null));
    }

    ProductionExceptionHandler(final Clock clock,
                               final Duration exceptionThresholdInterval,
                               final int exceptionThresholdCount) {
        super(clock, exceptionThresholdInterval, exceptionThresholdCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Map<String, ?> configs) {
        // Configuration is done via Quarkus config.
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

        if (exceedsThreshold()) {
            LOGGER.error("""
                    Failed to produce record to topic %s; \
                    Stopping to produce records, as the error was encountered %d times since %s, \
                    exceeding the configured threshold of %d occurrences in an interval of %s\
                    """
                    .formatted(record.topic(),
                            exceptionOccurrences(), firstExceptionOccurredAt(),
                            exceptionThresholdCount(), exceptionThresholdInterval()), exception);
            return ProductionExceptionHandlerResponse.FAIL;
        }

        LOGGER.warn("""
                Failed to produce record to topic %s; \
                Skipping and continuing to produce records, as the configured threshold of \
                %d occurrences in an interval of %s has not been exceeded yet\
                """
                .formatted(record.topic(), exceptionThresholdCount(), exceptionThresholdInterval()), exception);
        return ProductionExceptionHandlerResponse.CONTINUE;
    }

}
