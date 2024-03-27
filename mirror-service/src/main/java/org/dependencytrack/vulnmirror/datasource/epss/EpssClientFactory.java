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
package org.dependencytrack.vulnmirror.datasource.epss;

import io.github.jeremylong.openvulnerability.client.epss.EpssDataFeed;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class EpssClientFactory {

    private final EpssConfig epssConfig;

    EpssClientFactory(final EpssConfig epssConfig) {
        this.epssConfig = epssConfig;
    }

    EpssDataFeed create() {
        if (epssConfig.downloadUrl().isPresent() || !epssConfig.downloadUrl().isEmpty()) {
            return new EpssDataFeed(epssConfig.downloadUrl().get());
        }
        else {
            // using default location "https://epss.cyentia.com/epss_scores-current.csv.gz"
            return new EpssDataFeed();
        }
    }
}