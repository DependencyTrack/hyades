package org.hyades.vulnmirror.datasource.util;

import org.slf4j.Logger;

import java.lang.Thread.UncaughtExceptionHandler;

public class LoggingUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private final Logger logger;

    public LoggingUncaughtExceptionHandler(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        logger.error("An uncaught exception occurred while processing a task", e);
    }

}
