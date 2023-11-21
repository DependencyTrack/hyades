package org.dependencytrack.vulnmirror.datasource.util;

import org.slf4j.Logger;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {

    private final Logger logger;

    public LoggingRejectedExecutionHandler(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
        logger.warn("A task execution was rejected; Likely because the executor's task queue is already saturated");
    }

}
