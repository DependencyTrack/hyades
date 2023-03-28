package org.hyades.vulnmirror.datasource.nvd;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class NvdMirrorConfiguration {

    @Produces
    @ApplicationScoped
    @Named("nvdExecutorService")
    ExecutorService executorService() {
        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-nvd-%d")
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory);
    }

    @Produces
    @ApplicationScoped
    @Named("nvdDurationTimer")
    Timer durationTimer(final MeterRegistry meterRegistry) {
        return Timer.builder("mirror.nvd.duration")
                .description("Duration of NVD mirroring operations")
                .register(meterRegistry);
    }

}
