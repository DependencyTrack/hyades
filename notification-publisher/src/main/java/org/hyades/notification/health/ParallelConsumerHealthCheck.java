package org.hyades.notification.health;

import io.confluent.parallelconsumer.ParallelEoSStreamProcessor;
import io.confluent.parallelconsumer.ParallelStreamProcessor;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.hyades.proto.notification.v1.Notification;

import javax.enterprise.context.ApplicationScoped;

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
