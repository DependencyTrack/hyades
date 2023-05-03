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
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.hyades.notification.publisher;

import io.pebbletemplates.pebble.PebbleEngine;
import io.quarkus.runtime.Startup;
import org.hyades.persistence.ConfigPropertyRepository;
import org.hyades.proto.notification.v1.Level;
import org.hyades.proto.notification.v1.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import java.io.PrintStream;

@ApplicationScoped
@Startup // Force bean creation even though no direct injection points exist
public class ConsolePublisher implements Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePublisher.class);
    private static final PebbleEngine ENGINE = new PebbleEngine.Builder().newLineTrimming(false).build();

    final ConfigPropertyRepository configPropertyRepository;

    @Inject
    public ConsolePublisher(final ConfigPropertyRepository configPropertyRepository){
        this.configPropertyRepository = configPropertyRepository;
    }

    public void inform(final Notification notification, final JsonObject config) throws Exception {
        final String content = prepareTemplate(notification, getTemplate(config), configPropertyRepository, config);
        if (content == null) {
            LOGGER.warn("A template was not found. Skipping notification");
            return;
        }
        final PrintStream ps;
        if (notification.getLevel() == Level.LEVEL_ERROR) {
            ps = System.err;
        } else {
            ps = System.out;
        }
        ps.println(content);
    }

    @Override
    public PebbleEngine getTemplateEngine() {
        return ENGINE;
    }
}
