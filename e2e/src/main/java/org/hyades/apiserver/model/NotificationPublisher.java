package org.hyades.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationPublisher(UUID uuid, String name) {
}
