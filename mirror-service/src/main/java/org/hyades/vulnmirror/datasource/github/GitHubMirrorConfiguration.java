package org.hyades.vulnmirror.datasource.github;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class GitHubMirrorConfiguration {

    @Produces
    @ApplicationScoped
    @Named("githubExecutorService")
    ExecutorService executorService() {
        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-github-%d")
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory);
    }

    @Produces
    @ApplicationScoped
    @Named("githubDurationTimer")
    Timer durationTimer(final MeterRegistry meterRegistry) {
        return Timer.builder("mirror.github.duration")
                .description("Duration of GitHub mirroring operations")
                .register(meterRegistry);
    }

}
