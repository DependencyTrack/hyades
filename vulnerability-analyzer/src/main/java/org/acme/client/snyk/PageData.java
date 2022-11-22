package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageData<T>(String id, String type, T attributes) implements Serializable {
}
