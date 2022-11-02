package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageData<T>(String id, String type, T attributes) {
}
