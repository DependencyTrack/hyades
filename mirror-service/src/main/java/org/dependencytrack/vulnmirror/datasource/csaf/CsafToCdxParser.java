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
package org.dependencytrack.vulnmirror.datasource.csaf;

import com.google.protobuf.Timestamp;
import io.github.csaf.sbom.schema.generated.Csaf;
import io.github.csaf.sbom.schema.generated.Csaf.Id;
import org.cyclonedx.proto.v1_6.Bom;
import org.cyclonedx.proto.v1_6.Property;
import org.cyclonedx.proto.v1_6.Source;
import org.cyclonedx.proto.v1_6.Vulnerability;
import org.dependencytrack.vulnmirror.datasource.Datasource;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class takes care of converting a CSAF vulnerability entry to a CycloneDX vulnerability.
 */
public class CsafToCdxParser {
    private static final String TITLE_PROPERTY_NAME = "dependency-track:vuln:title";
    private static final Source SOURCE = Source.newBuilder().setName(Datasource.GITHUB.name()).build();

    public static Bom parse(Csaf.Vulnerability csafVuln, Csaf.Document csafDoc, int vulnIndex) {
        Vulnerability.Builder out = Vulnerability.newBuilder();

        out.setId(computeId(csafVuln, csafDoc, vulnIndex));
        out.setSource(SOURCE);

        Optional.ofNullable(csafVuln.getTitle())
                .ifPresent(title -> {
                    out.addProperties(
                            Property.newBuilder().setName(TITLE_PROPERTY_NAME).setValue(csafVuln.getTitle()).build()
                    );
                    out.setDescription(title);
                });

        out
                .setDetail(csafVuln.getNotes().stream().map((note) -> note.toString()).collect(Collectors.joining()))
                .setRecommendation("TODO");

        Optional.ofNullable(csafVuln.getRelease_date())
                .map(published -> Timestamp.newBuilder().setSeconds(published.getEpochSeconds()).build())
                .ifPresent(out::setPublished);

        Optional.ofNullable(csafVuln.getDiscovery_date())
                .map(created -> Timestamp.newBuilder().setSeconds(created.getEpochSeconds()).build())
                .ifPresent(out::setCreated);

        // out.setCredits(VulnerabilityCredits.newBuilder().addIndivi)

        // external links
        final StringBuilder sb = new StringBuilder();
        // if (!bom.getExternalReferencesList().isEmpty()) {
        //     bom.getExternalReferencesList().forEach(externalReference -> {
        //         sb.append("* [").append(externalReference.getUrl()).append("](").append(externalReference.getUrl())
        //                 .append(")\n");
        //     });
        //     vuln.setReferences(sb.toString());
        // }
        // if (!cycloneVuln.getAdvisoriesList().isEmpty()) {
        //     cycloneVuln.getAdvisoriesList().forEach(advisory -> {
        //         sb.append("* [").append(advisory.getUrl()).append("](").append(advisory.getUrl()).append(")\n");
        //     });
        //     vuln.setReferences(sb.toString());
        // }

        // Split CWE- from CWE ID string
        Optional.ofNullable(csafVuln.getCwe())
                .map(cwe -> Integer.parseInt(cwe.getId().split("-")[1]))
                .ifPresent(out::addCwes);

        // out.addRatings(null)

        // TODO add references

        return Bom.newBuilder().addVulnerabilities(out).build();
    }

    /**
     * This function tries to compute a (unique) ID for this {@link Csaf.Vulnerability},
     * so we can set it as an identifier for the {@link Vulnerability} in the {@link Bom}.
     *
     * @return a (hopefully) unique ID.
     */
    public static String computeId(Csaf.Vulnerability vuln, Csaf.Document doc, int vulnIndex) {
        // If we have a CVE, we can use that as the ID, but we want to prefix it with "CSAF"
        // to make it clear that it's a CSAF vulnerability.
        var cve = vuln.getCve();
        if (cve != null) {
            return "CSAF-" + cve;
        }

        // If there are unique IDs, we can just use them
        var ids = vuln.getIds();
        if (ids != null) {
            return "CSAF-" + ids.stream().map(Id::getText).collect(Collectors.joining("-"));
        }

        // Otherwise, we will use the tracking ID of the document and the index of the vulnerability
        return doc.getTracking().getId() + "-" + vulnIndex;
    }

}
