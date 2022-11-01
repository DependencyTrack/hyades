package org.acme.client.snyk;

import org.acme.model.Vulnerability;
import org.acme.util.JsonUtil;

import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import java.util.Optional;

public final class ModelConverter {

    private ModelConverter() {
    }

    public static Vulnerability convert(final Issue issue) {
        final var vuln = new Vulnerability();
        vuln.setVulnId(issue.key());
        vuln.setSource(Vulnerability.Source.SNYK);
        vuln.setTitle(issue.title());
        vuln.setDescription(issue.description());
        Optional.ofNullable(issue.createdAt())
                .map(JsonUtil::jsonStringToTimestamp)
                .map(ChronoZonedDateTime::toInstant)
                .map(Date::from)
                .ifPresent(vuln::setCreated);
        Optional.ofNullable(issue.updatedAt())
                .map(JsonUtil::jsonStringToTimestamp)
                .map(ChronoZonedDateTime::toInstant)
                .map(Date::from)
                .ifPresent(vuln::setUpdated);
        return vuln;
    }

}
