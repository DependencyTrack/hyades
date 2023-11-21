package org.dependencytrack.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationRule(UUID uuid, String name, boolean enabled) {
}
