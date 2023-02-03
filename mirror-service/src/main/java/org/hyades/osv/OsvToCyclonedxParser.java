package org.hyades.osv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.hyades.dto.OsvDto;
import org.hyades.model.Severity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.springett.cvss.Cvss;
import us.springett.cvss.Score;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.hyades.commonutil.VulnerabilityUtil.normalizedCvssV2Score;
import static org.hyades.commonutil.VulnerabilityUtil.normalizedCvssV3Score;
import static org.hyades.model.Severity.getSeverityByLevel;
import static org.hyades.commonutil.VulnerabilityUtil.fromString;
import static org.hyades.commonutil.VulnerabilityUtil.trimSummary;
import static org.hyades.util.ParserUtil.getBomRefIfComponentExists;

public class OsvToCyclonedxParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsvToCyclonedxParser.class);

    private final ObjectMapper objectMapper;

    @Inject
    public OsvToCyclonedxParser(@Named("osvObjectMapper") final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Bom parse(JSONObject object) {
        Objects.requireNonNull(object);

        var cyclonedxBom = new Bom();
        var severity = Vulnerability.Rating.Severity.UNKNOWN;
        var osvDto = convertStringToObject(object.toString(), OsvDto.class);

        // initial check if advisory is valid or withdrawn
        if (osvDto == null
                || (osvDto != null && osvDto.getWithdrawn() != null)) {
            return cyclonedxBom;
        }
        Vulnerability vulnerability = instantiateVulnerability(osvDto);
        if (osvDto.getDatabaseSpecific() != null) {
            vulnerability.setCwes(osvDto.getDatabaseSpecific().getCwes());
            severity = osvDto.getDatabaseSpecific().getSeverity();
        }
        vulnerability.setReferences(osvDto.getAliases());
        vulnerability.setCredits(osvDto.getCredits());
        vulnerability.setAdvisories(osvDto.getReferences().get("ADVISORY"));
        cyclonedxBom.setExternalReferences(osvDto.getReferences().get("EXTERNAL"));

        //affected ranges
        JSONArray osvAffectedArray = object.optJSONArray("affected");
        if (osvAffectedArray != null) {
            // affected packages and versions
            // low-priority severity assignment
            vulnerability.setAffects(parseAffectedRanges(osvAffectedArray, cyclonedxBom));
            severity = parseSeverity(osvAffectedArray);
        }

        // CVSS ratings
        vulnerability.setRatings(parseCvssRatings(object, severity));
        cyclonedxBom.setVulnerabilities(List.of(vulnerability));
        return cyclonedxBom;
    }

    private static List<Vulnerability.Affect> parseAffectedRanges(JSONArray osvAffectedArray, Bom bom) {
        PackageURL packageUrl;
        String ecoSystem = null;
        List<Vulnerability.Affect> affects = new ArrayList<>();

        for (int i = 0; i < osvAffectedArray.length(); i++) {
            JSONObject osvAffectedObj = osvAffectedArray.getJSONObject(i);
            String purl = parsePackageUrl(osvAffectedObj);
            try {
                packageUrl = new PackageURL(purl);
                ecoSystem = packageUrl.getType();
            } catch (MalformedPackageURLException ex) {
                LOGGER.info("Error while parsing purl: {}", purl, ex);
            }
            String bomReference = getBomRefIfComponentExists(bom, purl);
            if (bomReference == null) {
                Component component = createNewComponentWithPurl(osvAffectedObj, purl);
                bom.addComponent(component);
                bomReference = component.getBomRef();
            }
            Vulnerability.Affect versionRangeAffected = getAffectedPackageVersionRange(osvAffectedObj, ecoSystem);
            versionRangeAffected.setRef(bomReference);
            affects.add(versionRangeAffected);
        }
        return affects;
    }

    private static Vulnerability.Affect getAffectedPackageVersionRange(JSONObject osvAffectedObj, String ecoSystem) {

        // Ranges and Versions for each affected package
        JSONArray rangesArr = osvAffectedObj.optJSONArray("ranges");
        JSONArray versions = osvAffectedObj.optJSONArray("versions");
        var versionRangeAffected = new Vulnerability.Affect();
        List<Vulnerability.Version> versionRanges = new ArrayList<>();

        if (rangesArr != null) {
            rangesArr.forEach(item -> {
                var range = (JSONObject) item;
                versionRanges.addAll(generateRangeSpecifier(osvAffectedObj, range, ecoSystem));
            });
        }
        // if ranges are not available or only commit hash range is available, look for versions
        if (versions != null && versions.length() > 0) {
            var versionRange = new Vulnerability.Version();
            versionRange.setVersion(generateVersionSpecifier(versions, ecoSystem));
            versionRanges.add(versionRange);
        }
        versionRangeAffected.setVersions(versionRanges);
        return versionRangeAffected;
    }

    private static Vulnerability.Rating.Severity parseSeverity(JSONArray osvAffectedArray) {
        List<Integer> osvAffectedPackageSeverities = new ArrayList<>();
        osvAffectedArray.forEach(item -> {
            JSONObject osvAffectedObj = (JSONObject) item;
            JSONObject ecosystemSpecific = osvAffectedObj.optJSONObject("ecosystem_specific");
            JSONObject databaseSpecific = osvAffectedObj.optJSONObject("database_specific");
            osvAffectedPackageSeverities.add(
                    parseAffectedPackageSeverity(ecosystemSpecific, databaseSpecific).getLevel());
        });
        Collections.sort(osvAffectedPackageSeverities);
        Collections.reverse(osvAffectedPackageSeverities);
        return Vulnerability.Rating.Severity.fromString(
                String.valueOf(getSeverityByLevel(osvAffectedPackageSeverities.get(0))).toLowerCase());
    }

    private static Severity parseAffectedPackageSeverity(JSONObject ecosystemSpecific, JSONObject databaseSpecific) {

        String severity = null;
        if (databaseSpecific != null) {
            String cvssVector = databaseSpecific.optString("cvss", null);
            if (cvssVector != null) {
                Cvss cvss = Cvss.fromVector(cvssVector);
                Score score = cvss.calculateScore();
                severity = String.valueOf(normalizedCvssV3Score(score.getBaseScore()));
            }
        }
        if (severity == null && ecosystemSpecific != null) {
            severity = ecosystemSpecific.optString("severity", null);
        }
        return fromString(severity);
    }

    private static Component createNewComponentWithPurl(JSONObject osvAffectedObj, String purl) {
        JSONObject cpeObj = osvAffectedObj.optJSONObject("package");
        var component = new Component();
        UUID uuid = UUID.randomUUID();
        component.setBomRef(uuid.toString());
        component.setName(cpeObj.optString("name", null));
        component.setPurl(purl);
        return component;
    }

    private static String generateVersionSpecifier(JSONArray versions, String ecosystem) {
        String uniVersionRange = "vers:";
        if (ecosystem != null) {
            uniVersionRange += ecosystem;
        }
        uniVersionRange += "/";

        for (int i = 0; i < versions.length(); i++) {
            uniVersionRange += versions.getString(i) + "|";
        }
        return StringUtils.chop(uniVersionRange);
    }

    private static List<Vulnerability.Version> generateRangeSpecifier(JSONObject affectedRange, JSONObject range, String ecoSystem) {

        List<Vulnerability.Version> versionRanges = new ArrayList<>();
        String rangeType = range.optString("type");
        if (!"ECOSYSTEM".equalsIgnoreCase(rangeType) && !"SEMVER".equalsIgnoreCase(rangeType)) {
            // We can't support ranges of type GIT for now, as evaluating them requires knowledge of
            // the entire Git history of a package. We don't have that, so there's no point in ingesting this data.
            //
            // We're also implicitly excluding ranges of types that we don't yet know of.
            // This is a tradeoff of potentially missing new data vs. flooding our users' database with junk data.
            return List.of();
        }

        JSONArray rangeEvents = range.optJSONArray("events");
        if (rangeEvents == null) {
            return List.of();
        }

        for (int i = 0; i < rangeEvents.length(); i++) {
            JSONObject event = rangeEvents.getJSONObject(i);
            String introduced = event.optString("introduced", null);
            if (introduced == null) {
                // "introduced" is required for every range. But events are not guaranteed to be sorted,
                // it's merely a recommendation by the OSV specification.
                //
                // If events are not sorted, we have no way to tell what the correct order should be.
                // We make a tradeoff by assuming that ranges are sorted, and potentially skip ranges that aren't.
                continue;
            }
            var versionRange = new Vulnerability.Version();
            String uniVersionRange = "vers:";
            if (ecoSystem != null) {
                uniVersionRange += ecoSystem;
            }
            uniVersionRange += "/";
            uniVersionRange += ">=" + introduced + "|";

            if (i + 1 < rangeEvents.length()) {
                event = rangeEvents.getJSONObject(i + 1);
                String fixed = event.optString("fixed", null);
                String lastAffected = event.optString("last_affected", null);
                String limit = event.optString("limit", null);

                if (fixed != null) {
                    uniVersionRange += "<" + fixed + "|";
                    i++;
                } else if (lastAffected != null) {
                    uniVersionRange += "<=" + lastAffected + "|";
                    i++;
                } else if (limit != null) {
                    uniVersionRange += "<" + limit + "|";
                    i++;
                }
            }

            // Special treatment for GitHub: https://github.com/github/advisory-database/issues/470
            JSONObject databaseSpecific = affectedRange.optJSONObject("database_specific");
            if (databaseSpecific != null) {
                String lastAffectedRange = databaseSpecific.optString("last_known_affected_version_range", null);
                if (lastAffectedRange != null) {
                    uniVersionRange += lastAffectedRange;
                }
            }
            versionRange.setRange(StringUtils.chop(uniVersionRange));
            versionRanges.add(versionRange);
        }
        return versionRanges;
    }

    private static List<Vulnerability.Rating> parseCvssRatings(JSONObject object, Vulnerability.Rating.Severity severity) {
        List<Vulnerability.Rating> ratings = new ArrayList<>();
        JSONArray cvssList = object.optJSONArray("severity");

        if (cvssList == null) {
            var rating = new Vulnerability.Rating();
            rating.setSeverity(severity);
            ratings.add(rating);
            return ratings;
        }
        cvssList.forEach(item -> {
            JSONObject cvssObj = (JSONObject) item;
            String vector = cvssObj.optString("score", null);
            Cvss cvss = Cvss.fromVector(vector);

            var rating = new Vulnerability.Rating();
            double score = cvss.calculateScore().getBaseScore();
            rating.setVector(vector);
            rating.setScore(score);
            String type = cvssObj.optString("type", null);

            if (type != null && type.equalsIgnoreCase("CVSS_V3")) {
                rating.setMethod(Vulnerability.Rating.Method.CVSSV3);
                rating.setSeverity(Vulnerability.Rating.Severity.fromString(
                        String.valueOf(normalizedCvssV3Score(score)).toLowerCase()));
            } else {
                rating.setMethod(Vulnerability.Rating.Method.CVSSV2);
                rating.setSeverity(Vulnerability.Rating.Severity.fromString(
                        String.valueOf(normalizedCvssV2Score(score)).toLowerCase()));
            }
            ratings.add(rating);
        });
        return ratings;
    }

    private static Vulnerability instantiateVulnerability(OsvDto osvDto) {
        var vulnerability = new Vulnerability();
        vulnerability.setId(osvDto.getId());
        vulnerability.setDescription(trimSummary(osvDto.getSummary()));
        vulnerability.setDetail(osvDto.getDetails());
        vulnerability.setPublished(osvDto.getPublished());
        vulnerability.setUpdated(osvDto.getModified());
        return vulnerability;
    }

    private static String parsePackageUrl(JSONObject osvAffectedObj) {
        JSONObject cpeObj = osvAffectedObj.optJSONObject("package");
        return cpeObj != null ? cpeObj.optString("purl", null) : null;
    }

    private <T>T convertStringToObject(String stringToConvert, Class<T> type) {
        try {
           return this.objectMapper.readValue(stringToConvert, type);
        } catch (Exception ex) {
            LOGGER.error("Failed to parse Json object into Bom {}", ex);
        }
        return null;
    }
}
