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
import io.csaf.schema.generated.Csaf;
import io.csaf.schema.generated.Csaf.Id;
import org.apache.commons.codec.binary.Hex;
import org.cyclonedx.proto.v1_6.Bom;
import org.cyclonedx.proto.v1_6.OrganizationalContact;
import org.cyclonedx.proto.v1_6.OrganizationalEntity;
import org.cyclonedx.proto.v1_6.Property;
import org.cyclonedx.proto.v1_6.ScoreMethod;
import org.cyclonedx.proto.v1_6.Source;
import org.cyclonedx.proto.v1_6.Vulnerability;
import org.cyclonedx.proto.v1_6.VulnerabilityCredits;
import org.cyclonedx.proto.v1_6.VulnerabilityRating;
import org.cyclonedx.proto.v1_6.VulnerabilityReference;
import org.dependencytrack.vulnmirror.datasource.Datasource;
import org.dependencytrack.vulnmirror.datasource.util.ParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.springett.cvss.Cvss;
import us.springett.cvss.MalformedVectorException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class takes care of converting a CSAF vulnerability entry to a CycloneDX vulnerability.
 */
public class CsafToCdxParser {
    private static final String TITLE_PROPERTY_NAME = "dependency-track:vuln:title";
    private static final String PUBLISHERNAMESPACE_PROPERTY_NAME = "dependency-track:vuln:csaf:publisher";
    private static final String TRACKINGID_PROPERTY_NAME = "dependency-track:vuln:csaf:trackingId";
    private static final Source SOURCE = Source.newBuilder().setName(Datasource.CSAF.name()).build();
    private static final Logger LOGGER = LoggerFactory.getLogger(CsafToCdxParser.class);

    public static Bom parse(Csaf.Vulnerability csafVuln, Csaf.Document csafDoc, int vulnIndex) throws NoSuchAlgorithmException {
        Vulnerability.Builder out = Vulnerability.newBuilder();

        // Set ID and source
        out.setId(computeVulnerabilityId(csafVuln, csafDoc, vulnIndex));
        out.setSource(SOURCE);

        // Set some custom properties, we can use these to filter vulnerabilities later
        Optional.ofNullable(csafVuln.getTitle())
                .ifPresent(title -> {
                    out.addProperties(
                            Property.newBuilder().setName(TITLE_PROPERTY_NAME).setValue(csafVuln.getTitle()).build()
                    );
                });
        out.addProperties(Property.newBuilder()
                .setName(PUBLISHERNAMESPACE_PROPERTY_NAME)
                .setValue(csafDoc
                        .getPublisher()
                        .getNamespace()
                        .toString())
                .build());
        out.addProperties(Property.newBuilder()
                .setName(TRACKINGID_PROPERTY_NAME)
                .setValue(csafDoc
                        .getTracking()
                        .getId())
                .build());

        // Set details. We will use the first note with category "description" as the description.
        // All other notes will be added to the details.
        if (csafVuln.getNotes() != null) {
            var details = new StringBuilder();
            for (Csaf.Note note : csafVuln.getNotes()) {
                if (note.getCategory() == Csaf.Category.description) {
                    out.setDescription(note.getText());
                } else {
                    if (note.getTitle() != null) {
                        details.append("##### ").append(note.getTitle()).append("\n\n");
                    }
                    details.append(note.getText()).append("\n");
                }
            }
            if (!details.isEmpty()) {
                out.setDetail(details.toString());
            }
        }

        // Set the published and created timestamps
        Optional.ofNullable(csafVuln.getRelease_date())
                .map(published -> Timestamp.newBuilder().setSeconds(published.getEpochSeconds()).build())
                .ifPresent(out::setPublished);
        Optional.ofNullable(csafVuln.getDiscovery_date())
                .map(created -> Timestamp.newBuilder().setSeconds(created.getEpochSeconds()).build())
                .ifPresent(out::setCreated);

        // Set references to CVE entries
        Optional.ofNullable(csafVuln.getCve())
                .ifPresent(cve -> {
                    out.addReferences(VulnerabilityReference.newBuilder()
                            .setId(cve)
                            .setSource(Source.newBuilder().setName(Datasource.NVD.name())
                            .build()));
                        });

        // Set vulnerability scores (CVSS values)
        if (csafVuln.getScores() != null) {
            for (Csaf.Score score : csafVuln.getScores()) {
                Optional.ofNullable(score.getCvss_v2())
                        .flatMap(cvssV2 -> parseCvssVector(cvssV2.getVectorString(), ScoreMethod.SCORE_METHOD_CVSSV2))
                        .ifPresent(out::addRatings);

                Optional.ofNullable(score.getCvss_v3())
                        .flatMap(cvssV2 -> parseCvssVector(cvssV2.getVectorString(), ScoreMethod.SCORE_METHOD_CVSSV3))
                        .ifPresent(out::addRatings);
            }
        }

        // Set credits / acknowledgments
        var builder = VulnerabilityCredits.newBuilder();
        Optional.ofNullable(csafVuln.getAcknowledgments()).ifPresent(acks -> acks.forEach(ack -> {
            if(ack.getOrganization() != null) {
                builder.addOrganizations(OrganizationalEntity.newBuilder()
                        .setName(ack.getOrganization()).build());
            }

            if(ack.getNames() != null) {
                ack.getNames().forEach(name -> {
                    builder.addIndividuals(OrganizationalContact.newBuilder()
                            .setName(name).build());
                });
            }
        }));
        out.setCredits(builder.build());

        // Set CWE. by splitting "CWE-" from CWE ID string
        Optional.ofNullable(csafVuln.getCwe())
                .map(cwe -> Integer.parseInt(cwe.getId().split("-")[1]))
                .ifPresent(out::addCwes);

        return Bom.newBuilder().addVulnerabilities(out).build();
    }

    /**
     * This function tries to compute a unique ID of a @{link Csaf.Document} so we can set
     * it as a primary key for a {@link org.dependencytrack.persistence.model.CsafDocumentEntity}.
     * We use the prefix "CSAF" plus a truncated hash of the publisher namespace and the tracking ID.
     *
     * @param doc the doc
     * @return the ID
     */
    public static String computeDocumentId(Csaf.Document doc) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");

        return "CSAF-" + Hex.encodeHexString(
                digest.digest(
                        doc.getPublisher().getNamespace().toString().getBytes()
                )).substring(0, 8) + "-" + doc.getTracking().getId();
    }

    public static Optional<VulnerabilityRating> parseCvssVector(String vector, ScoreMethod method) {
        final Cvss cvss;
        try {
            cvss = Cvss.fromVector(vector);
            if (cvss == null) {
                return Optional.empty();
            }
        } catch (MalformedVectorException e) {
            LOGGER.warn("Failed to parse rating: CVSS vector {} is malformed; Skipping", vector, e);
            return Optional.empty();
        }

        return Optional.of(VulnerabilityRating.newBuilder()
                .setMethod(method)
                .setSource(SOURCE)
                .setVector(cvss.getVector())
                .setScore(cvss.calculateScore().getBaseScore())
                .setSeverity(ParserUtil.calculateCvssSeverity(cvss))
                .build());
    }

    /**
     * This function tries to compute a (unique) ID for this {@link Csaf.Vulnerability},
     * so we can set it as an identifier for the {@link Vulnerability} in the {@link Bom}.
     * As a prefix, the ID generated by {@link CsafToCdxParser.computeDocumentId} is used.
     *
     * @return a (hopefully) unique ID.
     */
    public static String computeVulnerabilityId(Csaf.Vulnerability vuln, Csaf.Document doc, int vulnIndex) throws NoSuchAlgorithmException {
        var prefix = computeDocumentId(doc);

        // If we have a CVE, we can use that as the ID
        var cve = vuln.getCve();
        if (cve != null) {
            return prefix + "-" + cve;
        }

        // If there are unique IDs, we can just use them
        var ids = vuln.getIds();
        if (ids != null) {
            return prefix + "-" + ids.stream().map(Id::getText).collect(Collectors.joining("-"));
        }

        // Otherwise, we will use the index of the vulnerability
        return prefix + "-VULNERABILITY" + vulnIndex;
    }

}
