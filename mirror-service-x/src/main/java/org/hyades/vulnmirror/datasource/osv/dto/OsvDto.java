package org.hyades.vulnmirror.datasource.osv.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.cyclonedx.proto.v1_4.Advisory;
import org.cyclonedx.proto.v1_4.ExternalReference;
import org.cyclonedx.proto.v1_4.OrganizationalContact;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.VulnerabilityCredits;
import org.cyclonedx.proto.v1_4.VulnerabilityReference;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hyades.commonutil.JsonUtil.jsonStringToTimestamp;
import static org.hyades.vulnmirror.datasource.util.ParserUtil.extractSource;

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
        List<Advisory> advisories = new ArrayList<>();

        this.references.forEach(reference -> {
            String url = reference.getUrl();
            if (reference.getType() != null && reference.getType().equalsIgnoreCase("ADVISORY")) {
                var advisory = Advisory.newBuilder()
                        .setUrl(url).build();
                advisories.add(advisory);
            } else {
                var externalReference = ExternalReference.newBuilder().setUrl(url).build();
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

    public VulnerabilityCredits getCredits() {
        if (this.credits == null) {
            return null;
        }
        var vulnerabilityCredits = VulnerabilityCredits.newBuilder();
        List<OrganizationalContact> creditArray = new ArrayList<>();
        this.credits.forEach(credit -> {
            var orgContact = OrganizationalContact.newBuilder();
            orgContact.setName(credit.getName());
            if (credit.getContact() != null) {
                String contactLink = String.join(";", credit.getContact());
                orgContact.setEmail(contactLink);
            }
            creditArray.add(orgContact.build());
        });
        vulnerabilityCredits.addAllIndividuals(creditArray);
        return vulnerabilityCredits.build();
    }

    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }

    public List<VulnerabilityReference> getAliases() {
        List<VulnerabilityReference> aliasReferences = new ArrayList<>();
        if (this.aliases == null) {
            return aliasReferences;
        }
        this.aliases.stream().forEach(alias -> {
            var reference = VulnerabilityReference.newBuilder()
                    .setId(alias)
                    .setSource(Source.newBuilder()
                            .setName(extractSource(alias))
                            .build())
                    .build();
            aliasReferences.add(reference);
        });
        return aliasReferences;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    @RegisterForReflection
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

    @RegisterForReflection
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

}

