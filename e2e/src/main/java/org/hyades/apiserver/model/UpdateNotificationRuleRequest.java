package org.hyades.apiserver.model;

import java.util.Set;
import java.util.UUID;

public record UpdateNotificationRuleRequest(UUID uuid, String name, boolean enabled, String notificationLevel,
                                            Set<String> notifyOn, String publisherConfig) {
}
