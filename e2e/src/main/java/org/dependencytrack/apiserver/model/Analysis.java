package org.dependencytrack.apiserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Analysis(@JsonProperty("analysisComments") List<Comment> comments) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Comment(String comment, String commenter) {
    }

}
