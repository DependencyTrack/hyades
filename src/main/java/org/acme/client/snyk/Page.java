package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Page<T>(PageMeta meta, List<PageData<T>> data) {
}
