package org.hyades.vulnmirror.datasource.osv;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class OsvMirrorConfiguration {

    @Produces
    @ApplicationScoped
    @Named("osvExecutorService")
    ExecutorService executorService() {
        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-osv-%d")
                .uncaughtExceptionHandler((thread, exception) -> {
                    final Logger logger = LoggerFactory.getLogger(OsvMirror.class);
                    logger.error("An uncaught exception was thrown while mirroring OSV", exception);
                })
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory);
    }

}
