package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Package(String type, String namespace, String name, String version, String url) {
}
