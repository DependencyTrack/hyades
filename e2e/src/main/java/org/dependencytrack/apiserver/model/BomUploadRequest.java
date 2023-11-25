package org.dependencytrack.apiserver.model;

public record BomUploadRequest(String projectName, String projectVersion, Boolean autoCreate, String bom) {
}
