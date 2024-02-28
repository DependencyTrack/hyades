package org.dependencytrack.vulnmirror.datasource.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.google.protobuf.Timestamp;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.Property;
import org.cyclonedx.proto.v1_4.ScoreMethod;
import org.cyclonedx.proto.v1_4.Severity;
import org.cyclonedx.proto.v1_4.Source;
import org.cyclonedx.proto.v1_4.Vulnerability;
import org.cyclonedx.proto.v1_4.VulnerabilityAffectedVersions;
import org.cyclonedx.proto.v1_4.VulnerabilityAffects;
import org.cyclonedx.proto.v1_4.VulnerabilityRating;
import org.dependencytrack.commonutil.VulnerabilityUtil;
import org.dependencytrack.vulnmirror.datasource.osv.dto.OsvDto;
import org.dependencytrack.vulnmirror.datasource.util.ParserUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.springett.cvss.Cvss;
import us.springett.cvss.Score;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.nscuro.versatile.VersUtils.versFromOsvRange;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_UNKNOWN;

public class OsvToCyclonedxParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsvToCyclonedxParser.class);
    private static final Pattern WILDCARD_VERS_PATTERN = Pattern.compile("^vers:\\w+/\\*$");
    private static final Pattern ZERO_VERSION_PATTERN = Pattern.compile("^0(\\.0)*$");
    private static final UUID UUID_V5_NAMESPACE = UUID.fromString("ffbefd63-724d-47b6-8d98-3deb06361885");

    private static final String TITLE_PROPERTY_NAME = "dependency-track:vuln:title";

    private final ObjectMapper objectMapper;

    @Inject
    public OsvToCyclonedxParser(@Named("osvObjectMapper") final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Bom parse(JSONObject object, boolean aliasSyncEnabled) {
        Objects.requireNonNull(object, "Json object cannot be null");

        Bom.Builder cyclonedxBom = Bom.newBuilder();
        var severity = SEVERITY_UNKNOWN;
        var osvDto = deserialize(object.toString(), OsvDto.class);

        // initial check if advisory is valid or withdrawn
        if (osvDto == null
                || (osvDto != null && osvDto.withdrawn() != null)) {
            return null;
        }
        Vulnerability.Builder vulnerability = buildVulnerability(osvDto);
        if (osvDto.databaseSpecific() != null) {
            vulnerability.addAllCwes(osvDto.databaseSpecific().getCwes());
            //this severity is compared with affected package severities and highest set
            severity = ParserUtil.mapSeverity(osvDto.databaseSpecific().severity());
        }
        if (aliasSyncEnabled) {
            vulnerability.addAllReferences(osvDto.getAliases());
        }
        Optional.ofNullable(osvDto.getCredits()).ifPresent(vulnerability::setCredits);
        Optional.ofNullable(osvDto.getReferences().get("ADVISORY")).ifPresent(vulnerability::addAllAdvisories);
        Optional.ofNullable(osvDto.getReferences().get("EXTERNAL")).ifPresent(cyclonedxBom::addAllExternalReferences);

        //affected ranges
        JSONArray osvAffectedArray = object.optJSONArray("affected");
        if (osvAffectedArray != null) {
            // affected packages and versions
            // low-priority severity assignment
            vulnerability.addAllAffects(parseAffectedRanges(vulnerability.getId(), osvAffectedArray, cyclonedxBom));
            severity = parseSeverity(osvAffectedArray);
        }

        // CVSS ratings
        vulnerability.addAllRatings(parseCvssRatings(object, severity));
        cyclonedxBom.addAllVulnerabilities(List.of(vulnerability.build()));
        return cyclonedxBom.build();
    }

    private static List<VulnerabilityAffects> parseAffectedRanges(final String vulnId, JSONArray osvAffectedArray, Bom.Builder bom) {
        PackageURL packageUrl;
        String ecoSystem = null;
        List<VulnerabilityAffects> affects = new ArrayList<>();

        for (int i = 0; i < osvAffectedArray.length(); i++) {
            JSONObject osvAffectedObj = osvAffectedArray.getJSONObject(i);
            String purl = parsePackageUrl(osvAffectedObj);
            if (purl == null) {
                LOGGER.debug("affected node at index {} for vulnerability {} does not provide a PURL; Skipping", i, vulnId);
                continue;
            }
            try {
                packageUrl = new PackageURL(purl);
                ecoSystem = packageUrl.getType();
            } catch (MalformedPackageURLException ex) {
                LOGGER.warn("Failed to parse PURL \"{}\" from affected node at index {} for vulnerability {}", purl, i, vulnId, ex);
                continue;
            }
            String bomReference = ParserUtil.getBomRefIfComponentExists(bom.build(), purl);
            if (bomReference == null) {
                Component component = createNewComponentWithPurl(osvAffectedObj, purl);
                bom.addComponents(component);
                bomReference = component.getBomRef();
            }
            VulnerabilityAffects versionRangeAffected = getAffectedPackageVersionRange(osvAffectedObj, ecoSystem);
            VulnerabilityAffects rangeWithBomReference = VulnerabilityAffects.newBuilder(versionRangeAffected)
                    .setRef(bomReference).build();
            affects.add(rangeWithBomReference);
        }
        return affects;
    }

    private static VulnerabilityAffects getAffectedPackageVersionRange(JSONObject osvAffectedObj, String ecoSystem) {

        // Ranges and Versions for each affected package
        JSONArray rangesArr = osvAffectedObj.optJSONArray("ranges");
        JSONArray versions = osvAffectedObj.optJSONArray("versions");
        var versionRangeAffected = VulnerabilityAffects.newBuilder();
        List<VulnerabilityAffectedVersions> versionRanges = new ArrayList<>();

        if (rangesArr != null) {
            rangesArr.forEach(item -> {
                var range = (JSONObject) item;
                versionRanges.addAll(generateRangeSpecifier(osvAffectedObj, range, ecoSystem));
            });
        }

        // OSV expands ranges into exact versions. While this is a nice service to offer, it means
        // that we'll get duplicate data if we consume both the ranges and exact versions.
        //
        // On the other hand, there are cases like https://osv-vulnerabilities.storage.googleapis.com/npm/MAL-2023-995.json,
        // where the range is expressing a `>=0` constraint, but an exact version (`103.99.99`) is provided.
        // Consuming only the range, or both range and exact version will yield false positives.
        //
        // Thus, we only consume exact versions when either:
        //   * No ranges could be parsed at all
        //   * Only wildcard ranges (`>=0`) were parsed
        // In the latter case, wildcard ranges will be dropped in favor of the exact versions.
        final boolean hasOnlyWildcardRanges = versionRanges.stream()
                .map(VulnerabilityAffectedVersions::getRange)
                .allMatch(WILDCARD_VERS_PATTERN.asPredicate());
        if ((versionRanges.isEmpty() || hasOnlyWildcardRanges) && versions != null) {
            versionRanges.clear(); // Remove any existing wildcard ranges.

            versions.forEach(version -> {
                var versionRange = VulnerabilityAffectedVersions.newBuilder();
                versionRange.setVersion(String.valueOf(version));
                versionRanges.add(versionRange.build());
            });
        }
        versionRangeAffected.addAllVersions(versionRanges);
        return versionRangeAffected.build();
    }

    private static Severity parseSeverity(JSONArray osvAffectedArray) {
        List<Integer> osvAffectedPackageSeverities = new ArrayList<>();
        osvAffectedArray.forEach(item -> {
            JSONObject osvAffectedObj = (JSONObject) item;
            JSONObject ecosystemSpecific = osvAffectedObj.optJSONObject("ecosystem_specific");
            JSONObject databaseSpecific = osvAffectedObj.optJSONObject("database_specific");
            osvAffectedPackageSeverities.add(
                    parseAffectedPackageSeverity(ecosystemSpecific, databaseSpecific).getNumber());
        });
        Collections.sort(osvAffectedPackageSeverities);
        Collections.reverse(osvAffectedPackageSeverities);
        return ParserUtil.mapSeverity(
                String.valueOf(org.dependencytrack.common.model.Severity.getSeverityByLevel(osvAffectedPackageSeverities.get(0))));
    }

    private static Severity parseAffectedPackageSeverity(JSONObject ecosystemSpecific, JSONObject databaseSpecific) {

        String severity = null;
        if (databaseSpecific != null) {
            String cvssVector = databaseSpecific.optString("cvss", null);
            if (cvssVector != null) {
                Cvss cvss = Cvss.fromVector(cvssVector);
                Score score = cvss.calculateScore();
                severity = String.valueOf(VulnerabilityUtil.normalizedCvssV3Score(score.getBaseScore()));
            }
        }
        if (severity == null && ecosystemSpecific != null) {
            severity = ecosystemSpecific.optString("severity", null);
        }
        return ParserUtil.mapSeverity(severity);
    }

    private static Component createNewComponentWithPurl(JSONObject osvAffectedObj, String purl) {
        JSONObject packageObj = osvAffectedObj.optJSONObject("package");
        UUID uuid = Generators.nameBasedGenerator(UUID_V5_NAMESPACE).generate(purl);
        Component.Builder component = Component.newBuilder()
                .setBomRef(uuid.toString());
        Optional.ofNullable(packageObj.optString("name", null)).ifPresent(name -> component.setName(name));
        Optional.ofNullable(purl).ifPresent(packagePurl -> component.setPurl(packagePurl));

        return component.build();
    }

    private static List<VulnerabilityAffectedVersions> generateRangeSpecifier(JSONObject affectedRange, JSONObject range, String ecoSystem) {
        JSONArray rangeEvents = range.optJSONArray("events");
        if (rangeEvents == null) {
            return List.of();
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Map.Entry<String, String>> rangeEventList = rangeEvents.toList().stream()
                .map(rangeEvent -> (Map.Entry<String, String>) mapper.convertValue(rangeEvent, Map.Entry.class))
                .collect(Collectors.toList());

        final var versionRanges = new ArrayList<VulnerabilityAffectedVersions>();
        String rangeType = range.optString("type");
        String rangeEcosystem = affectedRange.optJSONObject("package").optString("ecosystem");
        var vers = versFromOsvRange(rangeType, rangeEcosystem, rangeEventList);
        versionRanges.add(VulnerabilityAffectedVersions.newBuilder().setRange(String.valueOf(vers)).build());
        return versionRanges;
    }

    private static List<VulnerabilityRating> parseCvssRatings(JSONObject object, Severity severity) {
        List<VulnerabilityRating> ratings = new ArrayList<>();
        JSONArray cvssList = object.optJSONArray("severity");

        if (cvssList == null) {
            var rating = VulnerabilityRating.newBuilder()
                    .setSeverity(severity).build();
            ratings.add(rating);
            return ratings;
        }
        cvssList.forEach(item -> {
            JSONObject cvssObj = (JSONObject) item;
            String vector = cvssObj.optString("score", null);
            Cvss cvss = Cvss.fromVector(vector);

            var rating = VulnerabilityRating.newBuilder();
            double score = cvss.calculateScore().getBaseScore();
            rating.setVector(vector);
            rating.setScore(Double.parseDouble(NumberFormat.getInstance().format(score)));
            String type = cvssObj.optString("type", null);

            if (type != null && type.equalsIgnoreCase("CVSS_V3")) {
                rating.setMethod(ScoreMethod.SCORE_METHOD_CVSSV3);
                rating.setSeverity(ParserUtil.mapSeverity(
                        String.valueOf(VulnerabilityUtil.normalizedCvssV3Score(score))));
            } else {
                rating.setMethod(ScoreMethod.SCORE_METHOD_CVSSV2);
                rating.setSeverity(ParserUtil.mapSeverity(
                        String.valueOf(VulnerabilityUtil.normalizedCvssV2Score(score))));
            }
            ratings.add(rating.build());
        });
        return ratings;
    }

    private static Vulnerability.Builder buildVulnerability(OsvDto osvDto) {
        Vulnerability.Builder vulnerability = Vulnerability.newBuilder();
        Optional.ofNullable(osvDto.id()).ifPresent(id -> vulnerability.setId(id));
        vulnerability.setSource(extractSource(osvDto.id()));
        Optional.ofNullable(osvDto.summary()).ifPresent(summary -> vulnerability.addProperties(
                Property.newBuilder().setName(TITLE_PROPERTY_NAME).setValue(VulnerabilityUtil.trimSummary(summary)).build()));
        Optional.ofNullable(osvDto.details()).ifPresent(details -> vulnerability.setDescription(details));

        Optional.ofNullable(osvDto.getPublished())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(vulnerability::setPublished);

        Optional.ofNullable(osvDto.getModified())
                .map(published -> published.toInstant())
                .map(instant -> Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .ifPresent(vulnerability::setUpdated);

        return vulnerability;
    }

    private static String parsePackageUrl(JSONObject osvAffectedObj) {
        JSONObject packageObj = osvAffectedObj.optJSONObject("package");
        return packageObj != null ? packageObj.optString("purl", null) : null;
    }

    private <T> T deserialize(String stringToConvert, Class<T> type) {
        try {
            return this.objectMapper.readValue(stringToConvert, type);
        } catch (Exception ex) {
            LOGGER.error("Failed to parse Json object into Bom {}", ex);
        }
        return null;
    }

    private static Source extractSource(String vulnId) {
        final String sourceId = vulnId.split("-")[0];
        var source = Source.newBuilder();
        return switch (sourceId) {
            case "GHSA" -> source.setName("GITHUB").build();
            case "CVE" -> source.setName("NVD").build();
            default -> source.setName("OSV").build();
        };
    }
}

