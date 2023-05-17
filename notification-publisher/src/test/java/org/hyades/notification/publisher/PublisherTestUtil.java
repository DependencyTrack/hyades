package org.hyades.notification.publisher;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class PublisherTestUtil {

    static JsonObject getConfig(String publisher, String destination) {
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, "testType")
                .add(Publisher.CONFIG_TEMPLATE_KEY, getTemplateContent(publisher))
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(getExtraConfig())
                .build();
    }

    static String getTemplateContent(String notificationPublisher) {
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
                    "    \"timestamp\": \"{{ notification.timestamp }}\",\n" +
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
}
