package org.acme.client.ossindex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComponentReport(String coordinates, String description, String reference,
                              List<ComponentReportVulnerability> vulnerabilities) {
}
