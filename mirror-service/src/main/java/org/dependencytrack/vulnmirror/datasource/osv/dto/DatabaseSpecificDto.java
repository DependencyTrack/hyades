package org.dependencytrack.vulnmirror.datasource.osv.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.dependencytrack.common.cwe.CweResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatabaseSpecificDto(String severity, String url, @JsonProperty("cwe_ids") List<String> cwes, String source) implements Serializable {

    public List<Integer> getCwes() {
        List<Integer> cweIds = new ArrayList<>();
        if(this.cwes == null) {
            return cweIds;
        }
        this.cwes.forEach(cwe -> cweIds.add(CweResolver.getInstance().parseCweString(cwe)));
        return cweIds;
    }
}

