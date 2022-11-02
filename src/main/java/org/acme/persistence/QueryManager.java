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

import alpine.common.util.BooleanUtil;
import alpine.event.framework.Event;
import alpine.model.ApiKey;
import alpine.model.ConfigProperty;
import alpine.model.Team;
import alpine.model.UserPrincipal;
import alpine.notification.NotificationLevel;
import alpine.persistence.AlpineQueryManager;
import alpine.persistence.PaginatedResult;
import alpine.resources.AlpineRequest;
import com.github.packageurl.PackageURL;
import org.acme.event.IndexEvent;
import org.acme.model.*;
import org.acme.notification.NotificationScope;
import org.acme.notification.publisher.Publisher;
import org.acme.tasks.scanners.AnalyzerIdentity;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.json.JsonObject;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * This QueryManager provides a concrete extension of {@link AlpineQueryManager} by
 * providing methods that operate on the Dependency-Track specific models.
 *
 * @author Steve Springett
 * @since 3.0.0
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class QueryManager extends AlpineQueryManager {

    private AlpineRequest request;
    private NotificationQueryManager notificationQueryManager;


    /**
     * Default constructor.
     */
    public QueryManager() {
        super();
    }

    /**
     * Constructs a new QueryManager.
     * @param pm a PersistenceManager object
     */
    public QueryManager(final PersistenceManager pm) {
        super(pm);
    }

    /**
     * Constructs a new QueryManager.
     * @param request an AlpineRequest object
     */
    public QueryManager(final AlpineRequest request) {
        super(request);
        this.request = request;
    }

    /**
     * Constructs a new QueryManager.
     * @param request an AlpineRequest object
     */
    public QueryManager(final PersistenceManager pm, final AlpineRequest request) {
        super(pm, request);
        this.request = request;
    }

    /**
     * Lazy instantiation of ProjectQueryManager.
     * @return a ProjectQueryManager object
     */

    /**
     * Lazy instantiation of NotificationQueryManager.
     * @return a NotificationQueryManager object
     */
    private NotificationQueryManager getNotificationQueryManager() {
        if (notificationQueryManager == null) {
            notificationQueryManager = (request == null) ? new NotificationQueryManager(getPersistenceManager()) : new NotificationQueryManager(getPersistenceManager(), request);
        }
        return notificationQueryManager;
    }

    public NotificationRule createNotificationRule(String name, NotificationScope scope, NotificationLevel level, NotificationPublisher publisher) {
        return getNotificationQueryManager().createNotificationRule(name, scope, level, publisher);
    }

    public NotificationRule updateNotificationRule(NotificationRule transientRule) {
        return getNotificationQueryManager().updateNotificationRule(transientRule);
    }

    public PaginatedResult getNotificationRules() {
        return getNotificationQueryManager().getNotificationRules();
    }

    public List<NotificationPublisher> getAllNotificationPublishers() {
        return getNotificationQueryManager().getAllNotificationPublishers();
    }

    public NotificationPublisher getNotificationPublisher(final String name) {
        return getNotificationQueryManager().getNotificationPublisher(name);
    }

    public NotificationPublisher getDefaultNotificationPublisher(final Class<Publisher> clazz) {
        return getNotificationQueryManager().getDefaultNotificationPublisher(clazz);
    }

    public NotificationPublisher createNotificationPublisher(final String name, final String description,
                                                             final Class<Publisher> publisherClass, final String templateContent,
                                                             final String templateMimeType, final boolean defaultPublisher) {
        return getNotificationQueryManager().createNotificationPublisher(name, description, publisherClass, templateContent, templateMimeType, defaultPublisher);
    }

    public NotificationPublisher updateNotificationPublisher(NotificationPublisher transientPublisher) {
        return getNotificationQueryManager().updateNotificationPublisher(transientPublisher);
    }

    public void deleteNotificationPublisher(NotificationPublisher notificationPublisher) {
        getNotificationQueryManager().deleteNotificationPublisher(notificationPublisher);
    }

    public void removeProjectFromNotificationRules(final Project project) {
        getNotificationQueryManager().removeProjectFromNotificationRules(project);
    }

    public void removeTeamFromNotificationRules(final Team team) {
        getNotificationQueryManager().removeTeamFromNotificationRules(team);
    }

    /**
     * Determines if a config property is enabled or not.
     * @param configPropertyConstants the property to query
     * @return true if enabled, false if not
     */
    public boolean isEnabled(final ConfigPropertyConstants configPropertyConstants) {
        final ConfigProperty property = getConfigProperty(
                configPropertyConstants.getGroupName(), configPropertyConstants.getPropertyName()
        );
        if (property != null && ConfigProperty.PropertyType.BOOLEAN == property.getPropertyType()) {
            return BooleanUtil.valueOf(property.getPropertyValue());
        }
        return false;
    }

    /**
     * Fetch an object from the datastore by its {@link UUID}, using the provided fetch groups.
     * <p>
     * {@code fetchGroups} will override any other fetch groups set on the {@link PersistenceManager},
     * even the default one. If inclusion of the default fetch group is desired, it must be
     * included in {@code fetchGroups} explicitly.
     * <p>
     * Eventually, this may be moved to {@link alpine.persistence.AbstractAlpineQueryManager}.
     *
     * @param clazz Class of the object to fetch
     * @param uuid {@link UUID} of the object to fetch
     * @param fetchGroups Fetch groups to use for this operation
     * @return The object if found, otherwise {@code null}
     * @param <T> Type of the object
     * @throws Exception When closing the query failed
     * @since 4.6.0
     */
    public <T> T getObjectByUuid(final Class<T> clazz, final UUID uuid, final List<String> fetchGroups) throws Exception {
        try (final Query<T> query = pm.newQuery(clazz)) {
            query.setFilter("uuid == :uuid");
            query.setParameters(uuid);
            query.getFetchPlan().setGroups(fetchGroups);
            return query.executeUnique();
        }
    }

    /**
     * Convenience method to execute a given {@link Runnable} within the context of a {@link Transaction}.
     * <p>
     * Eventually, this may be moved to {@link alpine.persistence.AbstractAlpineQueryManager}.
     *
     * @param runnable The {@link Runnable} to execute
     * @since 4.6.0
     */
    public void runInTransaction(final Runnable runnable) {
        final Transaction trx = pm.currentTransaction();
        try {
            trx.begin();
            runnable.run();
            trx.commit();
        } finally {
            if (trx.isActive()) {
                trx.rollback();
            }
        }
    }

}
