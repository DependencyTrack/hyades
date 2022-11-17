package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Issue(String key, String type, String title, String description,
                    @JsonProperty("created_at") String createdAt, @JsonProperty("updated_at") String updatedAt,
                    List<Problem> problems, Slots slots, List<Severity> severities) {
}
