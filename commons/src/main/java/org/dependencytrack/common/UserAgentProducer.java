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

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

class UserAgentProducer {

    @Produces
    @Singleton
    @UserAgent
    String userAgent(@ConfigProperty(name = "quarkus.application.name") final String appName,
                     @ConfigProperty(name = "quarkus.application.version") final String appVersion,
                     @ConfigProperty(name = "dtrack.internal.cluster.id") final String clusterId) {
        return "%s v%s (%s; %s; %s) ManagedHttpClient/%s".formatted(appName, appVersion,
                System.getProperty("os.arch"), System.getProperty("os.name"), System.getProperty("os.version"), clusterId);
    }

}
