package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Severity(String source, String vector, Float score, String level) {
}
