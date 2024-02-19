package org.dependencytrack.vulnmirror.datasource.github;

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

class GitHubMirrorConfiguration {

    @Produces
    @ForGitHubMirror
    @ApplicationScoped
    ExecutorService executorService() {
        final Logger githubMirrorLogger = LoggerFactory.getLogger(GitHubMirror.class);

        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-github-%d")
                .uncaughtExceptionHandler(new LoggingUncaughtExceptionHandler(githubMirrorLogger))
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory, new LoggingRejectedExecutionHandler(githubMirrorLogger));
    }

    @Produces
    @ForGitHubMirror
    @ApplicationScoped
    Timer durationTimer(final MeterRegistry meterRegistry) {
        return Timer.builder("mirror.github.duration")
                .description("Duration of GitHub mirroring operations")
                .register(meterRegistry);
    }

}