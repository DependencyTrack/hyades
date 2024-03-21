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
package org.dependencytrack.notification.config;

import io.pebbletemplates.pebble.PebbleEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;
import org.dependencytrack.notification.template.extension.CustomExtension;

class PebbleConfiguration {

    @Produces
    @ApplicationScoped
    @Named("pebbleEngineJson")
    PebbleEngine pebbleEngineJson(final CustomExtension customExtension) {
        return new PebbleEngine.Builder()
                .extension(customExtension)
                .defaultEscapingStrategy("json")
                .build();
    }

    @Produces
    @ApplicationScoped
    @Named("pebbleEnginePlainText")
    PebbleEngine pebbleEnginePlainText(final CustomExtension customExtension) {
        return new PebbleEngine.Builder()
                .extension(customExtension)
                .newLineTrimming(false)
                .build();
    }

    @Produces
    @ApplicationScoped
    CustomExtension pebbleEngineExtension() {
        return new CustomExtension();
    }

}
