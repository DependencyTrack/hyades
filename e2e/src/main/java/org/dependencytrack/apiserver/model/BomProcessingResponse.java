package org.dependencytrack.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BomProcessingResponse(Boolean processing) {
}
