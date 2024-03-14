package org.dependencytrack.vulnmirror.datasource.epss;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.dependencytrack.vulnmirror.datasource.util.LoggingRejectedExecutionHandler;
import org.dependencytrack.vulnmirror.datasource.util.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class EpssMirrorConfiguration {

    @Produces
    @ForEpssMirror
    @ApplicationScoped
    ExecutorService executorService() {
        final Logger epssMirrorLogger = LoggerFactory.getLogger(EpssMirror.class);

        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-epss-%d")
                .uncaughtExceptionHandler(new LoggingUncaughtExceptionHandler(epssMirrorLogger))
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory, new LoggingRejectedExecutionHandler(epssMirrorLogger));
    }

    @Produces
    @ForEpssMirror
    @ApplicationScoped
    Timer durationTimer(final MeterRegistry meterRegistry) {
        return Timer.builder("mirror.epss.duration")
                .description("Duration of EPSS mirroring operations")
                .register(meterRegistry);
    }
}
