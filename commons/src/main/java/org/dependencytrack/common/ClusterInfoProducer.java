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
package org.dependencytrack.common;

import io.quarkus.arc.DefaultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.util.UUID;

class ClusterInfoProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterInfoProducer.class);

    @Produces
    @DefaultBean
    @Singleton
    ClusterInfo clusterInfo() {
        final var clusterInfo = new ClusterInfo(UUID.randomUUID().toString());
        LOGGER.warn("Not initialized from database, cluster info will be out of sync with other services: {}", clusterInfo);
        return clusterInfo;
    }

}
