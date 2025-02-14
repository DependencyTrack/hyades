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

public class CsafToCdxParser {
    private static final String TITLE_PROPERTY_NAME = "dependency-track:vuln:title";
    private static final Source SOURCE = Source.newBuilder().setName(Datasource.GITHUB.name()).build();

    public static Bom parse(Csaf.Vulnerability in) {
        Vulnerability.Builder out = Vulnerability.newBuilder();

        out.setId("CSAF-" + in.getIds().stream().map(Id::getText).collect(Collectors.joining()));
        out.setSource(SOURCE)
                .addProperties(
                        Property.newBuilder().setName(TITLE_PROPERTY_NAME).setValue(in.getTitle()).build()
                )
                .setDescription(in.getTitle()) // TODO tracking summary
                .setDetail(in.getNotes().stream().map((note) -> note.toString()).collect(Collectors.joining()))
                .setRecommendation("TODO")
                .setPublished(Timestamp.newBuilder().setSeconds(in.getRelease_date().getEpochSeconds()));

        Optional.ofNullable(in.getDiscovery_date())
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

        Optional.ofNullable(in.getCwe())
                        .map(cwe -> Integer.parseInt(cwe.getId()))
                                .ifPresent(out::addCwes);

        // out.addRatings(null)

        // TODO add references

        return Bom.newBuilder().addVulnerabilities(out).build();
    }

}
