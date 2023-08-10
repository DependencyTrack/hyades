package org.hyades.vulnmirror.datasource.osv;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hyades.vulnmirror.datasource.util.LoggingRejectedExecutionHandler;
import org.hyades.vulnmirror.datasource.util.LoggingUncaughtExceptionHandler;
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
        final Logger osvMirrorLogger = LoggerFactory.getLogger(OsvMirror.class);

        final var threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("hyades-mirror-osv-%d")
                .uncaughtExceptionHandler(new LoggingUncaughtExceptionHandler(osvMirrorLogger))
                .build();

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1), threadFactory, new LoggingRejectedExecutionHandler(osvMirrorLogger));
    }

}
