package org.hyades.vulnmirror.datasource.github;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
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
                .uncaughtExceptionHandler((thread, exception) -> {
                    final Logger logger = LoggerFactory.getLogger(GitHubMirror.class);
                    logger.error("An uncaught exception was thrown while mirroring GitHub Advisories", exception);
                })
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory);
    }

}
