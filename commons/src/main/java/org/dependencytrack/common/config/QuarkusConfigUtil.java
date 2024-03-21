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
package org.dependencytrack.common.config;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Optional;

public final class QuarkusConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusConfigUtil.class);

    private QuarkusConfigUtil() {
    }

    public static <T> Optional<T> getConfigMapping(final Class<T> clazz) {
        try {
            final var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
            return Optional.of(config.getConfigMapping(clazz));
        } catch (NoSuchElementException | IllegalStateException e) {
            // When running tests without @QuarkusTest, resolving of the ConfigMapping will not work.
            LOGGER.debug("Config mapping of class {} could not be resolved", clazz.getName(), e);
            return Optional.empty();
        }
    }

}
