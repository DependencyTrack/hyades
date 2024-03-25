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
package org.dependencytrack.notification.health;

import io.confluent.parallelconsumer.ParallelEoSStreamProcessor;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import org.dependencytrack.proto.notification.v1.Notification;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

/**
 * Basic liveness check for the Confluent Parallel Consumer.
 * <p>
 * To be replaced with official implementation once
 * <a href="https://github.com/confluentinc/parallel-consumer/pull/485">#485</a> is merged.
 */
@Liveness
@ApplicationScoped
public class ParallelConsumerHealthCheck implements HealthCheck {

    private final ParallelStreamProcessor<String, Notification> parallelConsumer;

    ParallelConsumerHealthCheck(final ParallelStreamProcessor<String, Notification> parallelConsumer) {
        this.parallelConsumer = parallelConsumer;
    }

    @Override
    public HealthCheckResponse call() {
        final HealthCheckResponseBuilder responseBuilder = HealthCheckResponse
                .named("parallel_consumer")
                .status(!parallelConsumer.isClosedOrFailed());

        if (parallelConsumer.isClosedOrFailed()
                && parallelConsumer instanceof final ParallelEoSStreamProcessor<?, ?> concreteParallelConsumer
                && concreteParallelConsumer.getFailureCause() != null) {
            responseBuilder.withData("failure_cause", concreteParallelConsumer.getFailureCause().getMessage());
        }

        return responseBuilder.build();
    }

}
