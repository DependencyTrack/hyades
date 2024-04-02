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
package org.dependencytrack.persistence;

import io.quarkus.runtime.Startup;
import org.dependencytrack.common.ClusterInfo;
import org.dependencytrack.persistence.model.ConfigProperty;
import org.dependencytrack.persistence.model.ConfigPropertyConstants;
import org.dependencytrack.persistence.repository.ConfigPropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

class ClusterInfoProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterInfoProducer.class);

    @Startup
    @Produces
    @Singleton
    ClusterInfo clusterInfo(final ConfigPropertyRepository configPropertyRepository) {
        final ConfigProperty clusterIdProperty = configPropertyRepository.findByGroupAndName(
                ConfigPropertyConstants.INTERNAL_CLUSTER_ID.getGroupName(),
                ConfigPropertyConstants.INTERNAL_CLUSTER_ID.getPropertyName()
        );
        if (clusterIdProperty == null
            || clusterIdProperty.getPropertyValue() == null
            || clusterIdProperty.getPropertyValue().isBlank()) {
            throw new IllegalStateException("""
                    Cluster ID not found in database. The cluster ID is populated upon first launch \
                    of the API server, please confirm whether it started successfully.""");
        }

        final var clusterInfo = new ClusterInfo(clusterIdProperty.getPropertyValue());
        LOGGER.info("Initialized from database: {}", clusterInfo);
        return clusterInfo;
    }

}
