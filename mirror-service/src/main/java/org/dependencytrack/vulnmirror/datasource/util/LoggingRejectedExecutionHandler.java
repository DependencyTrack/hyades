/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
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
