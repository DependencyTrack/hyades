package org.hyades.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.vulnerability.Vulnerability;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hyades.commonutil.JsonUtil.jsonStringToTimestamp;
import static org.hyades.model.Vulnerability.Source.GITHUB;
import static org.hyades.model.Vulnerability.Source.NVD;
import static org.hyades.model.Vulnerability.Source.OSV;

@RegisterForReflection
public class OsvDto implements Serializable {
    private String id;
    private String summary;
    private String details;
    private String published;
    private String modified;
    private String withdrawn;
    private List<String> aliases;
    private List<Credit> credits;
    private List<Reference> references;

    @JsonProperty(("database_specific"))
    private DatabaseSpecificDto databaseSpecific;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getPublished() {
        ZonedDateTime zonedDateTime = jsonStringToTimestamp(this.published);
        return zonedDateTime != null ? Date.from(zonedDateTime.toInstant()) : null;
    }
    public void setPublished(String published) {
        this.published = published;
    }
    public Date getModified() {
        ZonedDateTime zonedDateTime = jsonStringToTimestamp(this.modified);
        return zonedDateTime != null ? Date.from(zonedDateTime.toInstant()) : null;
    }
    public void setModified(String modified) {
        this.modified = modified;
    }
    public String getWithdrawn() {
        return withdrawn;
    }
    public void setWithdrawn(String withdrawn) {
        this.withdrawn = withdrawn;
    }
    public Map<String, List> getReferences() {
        if (this.references == null) {
            return Collections.emptyMap();
        }
        List<ExternalReference> externalReferences = new ArrayList<>();
        List<Vulnerability.Advisory> advisories = new ArrayList<>();

        this.references.forEach(reference -> {
            String url = reference.getUrl();
            if (reference.getType() != null && reference.getType().equalsIgnoreCase("ADVISORY")) {
                var advisory = new Vulnerability.Advisory();
                advisory.setUrl(url);
                advisories.add(advisory);
            } else {
                var externalReference = new ExternalReference();
                externalReference.setUrl(url);
                externalReferences.add(externalReference);
            }
        });
        return Map.of("ADVISORY", advisories, "EXTERNAL", externalReferences);
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public DatabaseSpecificDto getDatabaseSpecific() {
        return databaseSpecific;
    }

    public void setDatabaseSpecific(DatabaseSpecificDto databaseSpecific) {
        this.databaseSpecific = databaseSpecific;
    }

    public Vulnerability.Credits getCredits() {
        if (this.credits == null) {
            return null;
        }
        var vulnerabilityCredits = new Vulnerability.Credits();
        List<OrganizationalContact> creditArray = new ArrayList<>();
        this.credits.forEach(credit -> {
            var orgContact = new OrganizationalContact();
            orgContact.setName(credit.getName());
            if (credit.getContact() != null) {
                String contactLink = String.join(";", credit.getContact());
                orgContact.setEmail(contactLink);
            }
            creditArray.add(orgContact);
        });
        vulnerabilityCredits.setIndividuals(creditArray);
        return vulnerabilityCredits;
    }

    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }

    public List<Vulnerability.Reference> getAliases() {
        List<Vulnerability.Reference> aliasReferences = new ArrayList<>();
        if (this.aliases == null) {
            return Collections.emptyList();
        }
        this.aliases.stream().forEach(alias -> {
            var reference = new Vulnerability.Reference();
            reference.setId(alias);
            var aliasSource = new Vulnerability.Source();
            aliasSource.setName(extractSource(alias));
            reference.setSource(aliasSource);
            aliasReferences.add(reference);
        });
        return aliasReferences;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Credit {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getContact() {
            return contact;
        }

        public void setContact(List<String> contact) {
            this.contact = contact;
        }

        private List<String> contact;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Reference {
        private String type;
        private String url;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private static String extractSource(String vulnId) {
        String sourceId = vulnId.split("-")[0];
        return switch (sourceId) {
            case "GHSA" -> GITHUB.name();
            case "CVE" -> NVD.name();
            default -> OSV.name();
        };
    }
}
