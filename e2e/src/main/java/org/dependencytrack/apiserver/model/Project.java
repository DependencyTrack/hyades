package org.dependencytrack.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Project(UUID uuid, String name, String version) {
}
