package org.hyades.vulnmirror.datasource.osv.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.resolver.CweResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseSpecificDto implements Serializable {
    private String severity;
    private String url;
    @JsonProperty("cwe_ids")
    private List<String> cwes;
    private String source;
    public String getSeverity() {
        return this.severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Integer> getCwes() {
        List<Integer> cweIds = new ArrayList<>();
        if(this.cwes == null) {
            return cweIds;
        }
        this.cwes.forEach(cwe -> cweIds.add(CweResolver.getInstance().parseCweString(cwe)));
        return cweIds;
    }

    public void setCwes(List<String> cwes) {
        this.cwes = cwes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

