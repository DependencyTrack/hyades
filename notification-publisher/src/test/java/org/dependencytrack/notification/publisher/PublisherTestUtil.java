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
package org.dependencytrack.notification.publisher;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.dependencytrack.proto.notification.v1.Notification;

import java.io.IOException;

public class PublisherTestUtil {

    static JsonObject getConfig(String publisher, String destination) {
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, "testType")
                .add(Publisher.CONFIG_TEMPLATE_KEY, getTemplateContent(publisher))
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(getExtraConfig())
                .build();
    }

    public static String getTemplateContent(String notificationPublisher) {
        switch(notificationPublisher) {
            case "CONSOLE": return "--------------------------------------------------------------------------------\n" +
                    "Notification\n" +
                    "  -- timestamp: {{ timestamp }}\n" +
                    "  -- level:     {{ notification.level }}\n" +
                    "  -- scope:     {{ notification.scope }}\n" +
                    "  -- group:     {{ notification.group }}\n" +
                    "  -- title:     {{ notification.title }}\n" +
                    "  -- content:   {{ notification.content }}";

            case "WEBHOOK": return "{\n" +
                    "  \"notification\": {\n" +
                    "    \"level\": \"{{ notification.level | escape(strategy=\"json\") }}\",\n" +
                    "    \"scope\": \"{{ notification.scope | escape(strategy=\"json\") }}\",\n" +
                    "    \"group\": \"{{ notification.group | escape(strategy=\"json\") }}\",\n" +
                    "    \"timestamp\": \"{{ timestamp }}\",\n" +
                    "    \"title\": \"{{ notification.title | escape(strategy=\"json\") }}\",\n" +
                    "    \"content\": \"{{ notification.content | escape(strategy=\"json\") }}\",\n" +
                    "    \"subject\": {{ subjectJson | raw }}\n" +
                    "  }\n" +
                    "}";

            default: return "templateContent";
        }
    }

    static JsonObjectBuilder getExtraConfig() {
        return Json.createObjectBuilder();
    }

    public static PublishContext createPublisherContext(final Notification notification) throws IOException {
        return PublishContext.fromRecord(new ConsumerRecord<>("topic", 1, 2L, "key", notification));
    }

}
