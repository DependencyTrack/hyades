package org.hyades.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfigProperty(String groupName, String propertyName, String propertyValue) {
}
