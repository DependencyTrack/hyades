package org.hyades.common.metrics;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.runtime.Startup;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Startup
@ApplicationScoped
class ExtraJvmMetricsBinder {

    @Inject
    void bind(final MeterRegistry meterRegistry) {
        new ProcessMemoryMetrics().bindTo(meterRegistry);
        new ProcessThreadMetrics().bindTo(meterRegistry);
    }

}
