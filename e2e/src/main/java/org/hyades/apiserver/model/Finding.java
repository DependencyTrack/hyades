package org.hyades.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Finding(Component component, Project project, Vulnerability vulnerability) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Component(UUID uuid, String name, String version) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project(UUID uuid, String name, String version) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vulnerability(UUID uuid, String vulnId, String source) {
    }

}
