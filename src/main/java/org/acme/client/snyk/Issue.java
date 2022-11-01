package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Issue(String key, String type, String title, String description,
                    String createdAt, String updatedAt, List<Problem> problems,
                    List<Slot> slots, List<Severity> severities) {
}
