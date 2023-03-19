package org.hyades.apiserver.model;

import java.util.UUID;

public record CreateNotificationRuleRequest(String name, String scope, String notificationLevel, Publisher publisher) {

    public record Publisher(UUID uuid) {
    }

}
