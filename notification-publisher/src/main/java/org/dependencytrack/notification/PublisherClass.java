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
package org.dependencytrack.notification;

import org.apache.commons.lang3.StringUtils;
import org.dependencytrack.notification.publisher.Publisher;

public enum PublisherClass {

    SlackPublisher,
    MsTeamsPublisher,
    MattermostPublisher,
    SendMailPublisher,
    ConsolePublisher,
    WebhookPublisher,
    CsWebexPublisher,
    JiraPublisher;

    public static Class<?> getPublisherClass(String publisherClassName) throws ClassNotFoundException {
        final var classPath = Publisher.class.getPackageName() + ".";
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.SlackPublisher.name())){
            return Class.forName(classPath + PublisherClass.SlackPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.MsTeamsPublisher.name())){
            return Class.forName(classPath + PublisherClass.MsTeamsPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.MattermostPublisher.name())){
            return Class.forName(classPath + PublisherClass.MattermostPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.SendMailPublisher.name())){
            return Class.forName(classPath + PublisherClass.SendMailPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.ConsolePublisher.name())){
            return Class.forName(classPath + PublisherClass.ConsolePublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.WebhookPublisher.name())){
            return Class.forName(classPath + PublisherClass.WebhookPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.CsWebexPublisher.name())){
            return Class.forName(classPath + PublisherClass.CsWebexPublisher.name());
        }
        if (StringUtils.containsIgnoreCase(publisherClassName, PublisherClass.JiraPublisher.name())){
            return Class.forName(classPath + PublisherClass.JiraPublisher.name());
        }
        return null;
    }
}
