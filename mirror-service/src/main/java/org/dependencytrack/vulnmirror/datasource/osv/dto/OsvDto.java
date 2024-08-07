/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.vulnmirror.datasource.osv.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.cyclonedx.proto.v1_6.Advisory;
import org.cyclonedx.proto.v1_6.ExternalReference;
import org.cyclonedx.proto.v1_6.OrganizationalContact;
import org.cyclonedx.proto.v1_6.Source;
import org.cyclonedx.proto.v1_6.VulnerabilityCredits;
import org.cyclonedx.proto.v1_6.VulnerabilityReference;
import org.dependencytrack.commonutil.JsonUtil;
import org.dependencytrack.vulnmirror.datasource.util.ParserUtil;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsvDto(String id, String summary, String details, String published, String modified, String withdrawn,
                     List<String> aliases, List<Credit> credits, List<Reference> references, @JsonProperty("database_specific") DatabaseSpecificDto databaseSpecific) implements Serializable {

    public Date getPublished() {
        ZonedDateTime zonedDateTime = JsonUtil.jsonStringToTimestamp(this.published);
        return zonedDateTime != null ? Date.from(zonedDateTime.toInstant()) : null;
    }

    public Date getModified() {
        ZonedDateTime zonedDateTime = JsonUtil.jsonStringToTimestamp(this.modified);
        return zonedDateTime != null ? Date.from(zonedDateTime.toInstant()) : null;
    }

    public Map<String, List> getReferences() {
        if (this.references == null) {
            return Collections.emptyMap();
        }
        List<ExternalReference> externalReferences = new ArrayList<>();
        List<Advisory> advisories = new ArrayList<>();

        this.references.forEach(reference -> {
            String url = reference.url();
            if (reference.type() != null && reference.type().equalsIgnoreCase("ADVISORY")) {
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

    public VulnerabilityCredits getCredits() {
        if (this.credits == null) {
            return null;
        }
        var vulnerabilityCredits = VulnerabilityCredits.newBuilder();
        List<OrganizationalContact> creditArray = new ArrayList<>();
        this.credits.forEach(credit -> {
            var orgContact = OrganizationalContact.newBuilder();
            orgContact.setName(credit.name());
            if (credit.contact() != null) {
                String contactLink = String.join(";", credit.contact());
                orgContact.setEmail(contactLink);
            }
            creditArray.add(orgContact.build());
        });
        vulnerabilityCredits.addAllIndividuals(creditArray);
        return vulnerabilityCredits.build();
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
                            .setName(ParserUtil.extractSource(alias))
                            .build())
                    .build();
            aliasReferences.add(reference);
        });
        return aliasReferences;
    }

}

