package org.acme.client.ossindex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComponentReportRequest(Collection<String> coordinates) {
}
