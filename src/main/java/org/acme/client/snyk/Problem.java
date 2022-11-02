package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Problem(String id, String source, String url) {
}
