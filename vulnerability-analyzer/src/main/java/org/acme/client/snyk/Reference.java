package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Reference(String title, String url) implements Serializable {
}
