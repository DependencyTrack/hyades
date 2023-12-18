package org.dependencytrack.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Finding(Component component, UUID project, Vulnerability vulnerability, Attribution attribution,
                      Analysis analysis) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Component(UUID uuid, String name, String version) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Vulnerability(UUID uuid, String vulnId, String source) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attribution(String analyzerIdentity, String attributedOn, String alternateIdentifier,
                              String referenceUrl) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Analysis(String state, Boolean isSuppressed) {
    }

}
