package org.hyades.notification.publisher;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class PublisherTestUtil {

    static JsonObject getConfig(String destination) {
        return Json.createObjectBuilder()
                .add(Publisher.CONFIG_TEMPLATE_MIME_TYPE_KEY, "testType")
                .add(Publisher.CONFIG_TEMPLATE_KEY, "templateContent")
                .add(Publisher.CONFIG_DESTINATION, destination)
                .addAll(getExtraConfig())
                .build();
    }

    static JsonObjectBuilder getExtraConfig() {
        return Json.createObjectBuilder();
    }
}
