package org.dependencytrack.vulnmirror.datasource.csaf;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.proto.v1_6.Bom;
import org.cyclonedx.proto.v1_6.Property;
import org.cyclonedx.proto.v1_6.ScoreMethod;
import org.cyclonedx.proto.v1_6.Source;
import org.cyclonedx.proto.v1_6.Vulnerability;
import org.cyclonedx.proto.v1_6.VulnerabilityCredits;
import org.cyclonedx.proto.v1_6.VulnerabilityRating;
import org.dependencytrack.common.cwe.CweResolver;
import org.dependencytrack.commonutil.VulnerabilityUtil;

import com.google.protobuf.Timestamp;

import io.github.csaf.sbom.schema.generated.Csaf;
import io.github.csaf.sbom.schema.generated.Csaf.Acknowledgment;
import io.github.csaf.sbom.schema.generated.Csaf.Id;
import io.github.csaf.sbom.schema.generated.Csaf.Remediation;
import us.springett.cvss.Cvss;

public class CsafToCdxParser {
    private static final String TITLE_PROPERTY_NAME = "dependency-track:vuln:title";

    public static Bom parse(Csaf.Vulnerability in) {
        Vulnerability.Builder out = Vulnerability.newBuilder();

        out.setId("CSAF-" + in.getIds().stream().map(Id::getText).collect(Collectors.joining()));
        out.setSource(Source.newBuilder()
                .setName(null)
                .setUrl(null)
                .build());

        out.setProperties(0, Property.newBuilder().setName(TITLE_PROPERTY_NAME).setValue(in.getTitle()));

        out.setDescription(in.getTitle()); // TODO tracking summary

        out.setDetail(in.getNotes().stream().map((note) -> note.toString()).collect(Collectors.joining()));

        out.setRecommendation("TODO");

        out.setPublished(Timestamp.newBuilder().setSeconds(in.getRelease_date().toEpochSecond()));

        // out.setUpdated(null)

        out.setCreated(Timestamp.newBuilder().setSeconds(in.getDiscovery_date().toEpochSecond()));

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

        out.addCwes(Integer.parseInt(in.getCwe().getId()));

        // out.addRatings(null)

        // TODO add references

        return Bom.newBuilder().addVulnerabilities(out).build();
    }

}
