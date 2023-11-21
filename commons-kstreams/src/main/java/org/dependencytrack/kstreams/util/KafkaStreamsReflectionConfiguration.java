package org.dependencytrack.kstreams.util;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.kafka.streams.processor.internals.DefaultKafkaClientSupplier;

@SuppressWarnings("unused")
@RegisterForReflection(targets = {
        // Introduced in kafka-streams 3.5.0 and not recognized by Quarkus <= 3.1.x.
        // Can be removed once Quarkus' KStreams integration supports it.
        DefaultKafkaClientSupplier.class,
})
public class KafkaStreamsReflectionConfiguration {
}
