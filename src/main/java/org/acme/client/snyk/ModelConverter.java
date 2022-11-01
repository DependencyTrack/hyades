package org.acme.client.snyk;

import org.acme.model.Vulnerability;
import org.acme.util.JsonUtil;

import java.util.Date;

public final class ModelConverter {

    private ModelConverter() {
    }

    public static Vulnerability convert(final Issue issue) {
        final var vuln = new Vulnerability();
        vuln.setVulnId(issue.key());
        vuln.setSource(Vulnerability.Source.SNYK);
        vuln.setTitle(issue.title());
        vuln.setDescription(issue.description());
        vuln.setCreated(Date.from(JsonUtil.jsonStringToTimestamp(issue.createdAt()).toInstant()));
        vuln.setUpdated(Date.from(JsonUtil.jsonStringToTimestamp(issue.updatedAt()).toInstant()));

        return vuln;
    }

}
