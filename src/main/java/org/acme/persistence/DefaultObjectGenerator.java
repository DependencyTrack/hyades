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
package org.acme.persistence;

import alpine.common.logging.Logger;
import io.quarkus.arc.deployment.SyntheticBeansProcessor;
import org.acme.RequirementsVerifier;
import org.acme.model.*;
import org.acme.notification.publisher.DefaultNotificationPublishers;
import org.acme.util.NotificationUtil;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates default objects on an empty database.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@WebListener
public class DefaultObjectGenerator implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(DefaultObjectGenerator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        LOGGER.info("Initializing default object generator");
        if (RequirementsVerifier.failedValidation()) {
            return;
        }
        //loadDefaultConfigProperties();
        //loadDefaultNotificationPublishers();

        try {
            new CweImporter().processCweDefinitions();
        } catch (Exception e) {
            LOGGER.error("Error adding CWEs to database");
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        /* Intentionally blank to satisfy interface */
    }
    /**
     * Loads the default ConfigProperty objects
     */
    /*private void loadDefaultConfigProperties() {
       *//* try (QueryManager qm = new QueryManager()) {
            LOGGER.info("Synchronizing config properties to datastore");
            for (final ConfigPropertyConstants cpc : ConfigPropertyConstants.values()) {
                LOGGER.debug("Creating config property: " + cpc.getGroupName() + " / " + cpc.getPropertyName());
                if (qm.getConfigProperty(cpc.getGroupName(), cpc.getPropertyName()) == null) {
                    qm.createConfigProperty(cpc.getGroupName(), cpc.getPropertyName(), cpc.getDefaultPropertyValue(), cpc.getPropertyType(), cpc.getDescription());
                }
            }*//*
            // dispatch a call to PoC configuration endpoint
            //List<ConfigProperty> configProperties = qm.getConfigProperties();
            KafkaServiceUtil.postConfig(configProperties);

    }*/

    /**
     * Loads the default notification publishers
     */
    private void loadDefaultNotificationPublishers() {
        NotificationHibernateManager nm = new NotificationHibernateManager();
            LOGGER.info("Synchronizing notification publishers to datastore");
            for (final DefaultNotificationPublishers publisher : DefaultNotificationPublishers.values()) {
                System.out.println(nm.getDefaultNotificationPublisher(publisher.getPublisherClass()));

            }
    }
}
