package org.acme.osv;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.acme.model.Severity;
import org.acme.resolver.CweResolver;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.springett.cvss.Cvss;
import us.springett.cvss.Score;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.acme.commonutil.JsonUtil.jsonStringToTimestamp;
import static org.acme.commonutil.VulnerabilityUtil.normalizedCvssV2Score;
import static org.acme.commonutil.VulnerabilityUtil.normalizedCvssV3Score;
import static org.acme.model.Severity.getSeverityByLevel;
import static org.acme.model.Vulnerability.Source.GITHUB;
import static org.acme.model.Vulnerability.Source.NVD;
import static org.acme.model.Vulnerability.Source.OSV;

public class OsvToCyclonedxParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsvToCyclonedxParser.class);
    final Bom cyclonedxBom;
    String ecosystem;
    Vulnerability.Rating.Severity severity = Vulnerability.Rating.Severity.UNKNOWN;

    public OsvToCyclonedxParser() {
        this.cyclonedxBom = new Bom();
        this.ecosystem = null;
    }

    public Bom parse(final JSONObject object) {

        // initial check if advisory is valid or withdrawn
        String withdrawn = object.optString("withdrawn", null);

        if(object != null && withdrawn == null) {

            Vulnerability vulnerability = new Vulnerability();
            vulnerability.setId(object.optString("id", null));
            vulnerability.setDescription(trimSummary(object.optString("summary", null)));
            vulnerability.setDetail(object.optString("details", null));
            ZonedDateTime published = jsonStringToTimestamp(object.optString("published", null));
            if (published != null) {
                vulnerability.setPublished(Date.from(published.toInstant()));
            }
            ZonedDateTime modified = jsonStringToTimestamp(object.optString("modified", null));
            if (modified != null) {
                vulnerability.setUpdated(Date.from(modified.toInstant()));
            }

            // ADVISORY link and external references
            final JSONArray references = object.optJSONArray("references");
            if (references != null) {
                List<Vulnerability.Advisory> advisories = new ArrayList<>();
                List<ExternalReference> externalReferences = new ArrayList<>();
                for (int i=0; i<references.length(); i++) {
                    final JSONObject reference = references.getJSONObject(i);
                    String referenceType = reference.optString("type", null);
                    String url = reference.optString("url", null);
                    if ("ADVISORY".equalsIgnoreCase(referenceType)) {
                        Vulnerability.Advisory advisory = new Vulnerability.Advisory();
                        advisory.setUrl(url);
                        advisories.add(advisory);
                    } else {
                        ExternalReference externalReference = new ExternalReference();
                        externalReference.setUrl(url);
                        externalReferences.add(externalReference);
                    }
                }
                vulnerability.setAdvisories(advisories);
                this.cyclonedxBom.setExternalReferences(externalReferences);
            }

            // CREDITS
            final JSONArray creditsObj = object.optJSONArray("credits");
            if (creditsObj != null) {
                vulnerability.setCredits(parseCredits(creditsObj));
            }

            // ALIASES
            final JSONArray aliases = object.optJSONArray("aliases");
            if(aliases != null) {
                vulnerability.setReferences(parseAliases(aliases));
            }

            final JSONArray osvAffectedArray = object.optJSONArray("affected");
            if (osvAffectedArray != null) {

                // CPE
                // AFFECTED PACKAGES AND VERSIONS
                // LOW-PRIORITY SEVERITY ASSIGNMENT
                vulnerability.setAffects(parseAffectedRanges(osvAffectedArray));
            }

            final JSONObject databaseSpecific = object.optJSONObject("database_specific");
            if (databaseSpecific != null) {

                // HIGH-PRIORITY SEVERITY ASSIGNMENT
                String osvSeverity = databaseSpecific.optString("severity", null);
                if (osvSeverity != null) {
                    severity = Vulnerability.Rating.Severity.fromString(osvSeverity.toLowerCase());
                }

                // CWEs
                final JSONArray osvCweIds = databaseSpecific.optJSONArray("cwe_ids");
                if(osvCweIds != null) {
                    vulnerability.setCwes(parseCwes(osvCweIds));
                }
            }

            // CVSS ratings
            vulnerability.setRatings(parseCvssRatings(object, severity));

            this.cyclonedxBom.setVulnerabilities(List.of(vulnerability));
        }

        return this.cyclonedxBom;
    }

    public List<Vulnerability.Affect> parseAffectedRanges(JSONArray osvAffectedArray) {

        List<Vulnerability.Affect> affects = new ArrayList<>();
        List<Integer> osvAffectedPackageSeverities = new ArrayList<>();

        for(int i=0; i<osvAffectedArray.length(); i++) {

            JSONObject osvAffectedObj = osvAffectedArray.getJSONObject(i);

            // CPE for each affected package
            String bomReference = parseCpe(osvAffectedObj);

            // Extract affected package severity
            final JSONObject ecosystemSpecific = osvAffectedObj.optJSONObject("ecosystem_specific");
            final JSONObject databaseSpecific = osvAffectedObj.optJSONObject("database_specific");
            osvAffectedPackageSeverities.add(
                    parseAffectedPackageSeverity(ecosystemSpecific, databaseSpecific).getLevel());

            // RANGES and VERSIONS for each affected package
            final JSONArray rangesObj = osvAffectedObj.optJSONArray("ranges");
            final JSONArray versions = osvAffectedObj.optJSONArray("versions");
            Vulnerability.Affect versionRangeAffected = new Vulnerability.Affect();
            versionRangeAffected.setRef(bomReference);
            List<Vulnerability.Version> versionRanges = new ArrayList<>();

            if (rangesObj != null) {
                for (int j=0; j<rangesObj.length(); j++) {
                    versionRanges.addAll(generateRangeSpecifier(osvAffectedObj, rangesObj.getJSONObject(j)));
                }
            }
            // if ranges are not available or only commit hash range is available, look for versions
            if (versions != null && versions.length() > 0) {
                Vulnerability.Version versionRange = new Vulnerability.Version();
                versionRange.setVersion(generateVersionSpecifier(versions));
                versionRanges.add(versionRange);
            }
            versionRangeAffected.setVersions(versionRanges);
            affects.add(versionRangeAffected);
        }

        Collections.sort(osvAffectedPackageSeverities);
        Collections.reverse(osvAffectedPackageSeverities);
        severity = Vulnerability.Rating.Severity.fromString(
                String.valueOf(getSeverityByLevel(osvAffectedPackageSeverities.get(0))).toLowerCase());

        return affects;
    }

    private Severity parseAffectedPackageSeverity(JSONObject ecosystemSpecific, JSONObject databaseSpecific) {

        String severity = null;

        if (databaseSpecific != null) {
            String cvssVector = databaseSpecific.optString("cvss", null);
            if (cvssVector != null) {
                Cvss cvss = Cvss.fromVector(cvssVector);
                Score score = cvss.calculateScore();
                severity = String.valueOf(normalizedCvssV3Score(score.getBaseScore()));
            }
        }

        if(severity == null && ecosystemSpecific != null) {
            severity = ecosystemSpecific.optString("severity", null);
        }

        if (severity != null) {
            if (severity.equalsIgnoreCase("CRITICAL")) {
                return Severity.CRITICAL;
            } else if (severity.equalsIgnoreCase("HIGH")) {
                return Severity.HIGH;
            } else if (severity.equalsIgnoreCase("MODERATE") || severity.equalsIgnoreCase("MEDIUM")) {
                return Severity.MEDIUM;
            } else if (severity.equalsIgnoreCase("LOW")) {
                return Severity.LOW;
            }
        }
        return Severity.UNASSIGNED;
    }

    private String parseCpe(JSONObject osvAffectedObj) {
        final JSONObject cpeObj = osvAffectedObj.optJSONObject("package");
        if (cpeObj != null) {
            String purl = cpeObj.optString("purl", null);
            if (purl != null && !purl.isBlank()) {
                try {
                    PackageURL packageURL = new PackageURL(purl);
                    ecosystem = packageURL.getType();
                } catch (MalformedPackageURLException ex) {
                    LOGGER.info("Error while parsing purl: " + purl, ex);
                }
            }
            if (this.cyclonedxBom.getComponents() != null) {
                Optional<Component> existingComponent = this.cyclonedxBom.getComponents().stream().filter(c ->
                        c.getPurl().equalsIgnoreCase(purl)).findFirst();
                if (existingComponent.isPresent()) {
                    return existingComponent.get().getBomRef();
                }
            }
            final Component component = new Component();
            final UUID uuid = UUID.randomUUID();
            component.setBomRef(uuid.toString());
            component.setName(cpeObj.optString("name", null));
            component.setPurl(purl);
            this.cyclonedxBom.addComponent(component);
            return uuid.toString();
        }
        return null;
    }

    private String generateVersionSpecifier(JSONArray versions) {
        String uniVersionRange = "vers:";
        if (ecosystem != null) {
            uniVersionRange += ecosystem;
        }
        uniVersionRange += "/";

        for (int i=0; i<versions.length(); i++) {
            uniVersionRange += versions.getString(i) + "|";
        }
        return StringUtils.chop(uniVersionRange);
    }

    private List<Vulnerability.Version> generateRangeSpecifier(JSONObject affectedRange, JSONObject range) {

        List<Vulnerability.Version> versionRanges = new ArrayList<>();

        final String rangeType = range.optString("type");
        if (!"ECOSYSTEM".equalsIgnoreCase(rangeType) && !"SEMVER".equalsIgnoreCase(rangeType)) {
            // We can't support ranges of type GIT for now, as evaluating them requires knowledge of
            // the entire Git history of a package. We don't have that, so there's no point in
            // ingesting this data.
            //
            // We're also implicitly excluding ranges of types that we don't yet know of.
            // This is a tradeoff of potentially missing new data vs. flooding our users'
            // database with junk data.
            return List.of();
        }

        final JSONArray rangeEvents = range.optJSONArray("events");
        if (rangeEvents == null) {
            return List.of();
        }

        for (int i = 0; i < rangeEvents.length(); i++) {

            JSONObject event = rangeEvents.getJSONObject(i);
            final String introduced = event.optString("introduced", null);
            if (introduced == null) {
                // "introduced" is required for every range. But events are not guaranteed to be sorted,
                // it's merely a recommendation by the OSV specification.
                //
                // If events are not sorted, we have no way to tell what the correct order should be.
                // We make a tradeoff by assuming that ranges are sorted, and potentially skip ranges
                // that aren't.
                continue;
            }
            final Vulnerability.Version versionRange = new Vulnerability.Version();
            String uniVersionRange = "vers:";
            if (ecosystem != null) {
                uniVersionRange += ecosystem;
            }
            uniVersionRange += "/";
            uniVersionRange += ">=" + introduced + "|";

            if (i + 1 < rangeEvents.length()) {
                event = rangeEvents.getJSONObject(i + 1);
                final String fixed = event.optString("fixed", null);
                final String lastAffected = event.optString("last_affected", null);
                final String limit = event.optString("limit", null);

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
            final JSONObject databaseSpecific = affectedRange.optJSONObject("database_specific");
            if (databaseSpecific != null) {
                final String lastAffectedRange = databaseSpecific.optString("last_known_affected_version_range", null);
                if (lastAffectedRange != null) {
                    uniVersionRange += lastAffectedRange;
                }
            }
            versionRange.setRange(StringUtils.chop(uniVersionRange));
            versionRanges.add(versionRange);
        }
        return versionRanges;
    }

    private Vulnerability.Credits parseCredits(JSONArray creditsObj) {
        Vulnerability.Credits credits = new Vulnerability.Credits();
        List<OrganizationalContact> creditArray = new ArrayList<>();
        for (int i=0; i<creditsObj.length(); i++) {
            OrganizationalContact credit = new OrganizationalContact();
            final JSONObject creditObj = creditsObj.getJSONObject(i);
            credit.setName(creditObj.optString("name", null));
            final JSONArray contact = creditObj.optJSONArray("contact");
            if (contact != null) {
                String contactLink = "";
                for (int j=0; j<creditsObj.length(); j++) {
                    contactLink += creditsObj.optString(j) + ";";
                }
                credit.setEmail(contactLink);
            }
            creditArray.add(credit);
        }
        credits.setIndividuals(creditArray);
        return credits;
    }

    private List<Vulnerability.Reference> parseAliases(JSONArray osvAliases) {
        List<Vulnerability.Reference> aliases = new ArrayList<>();
        for (int i=0; i<osvAliases.length(); i++) {
            String osvAlias = osvAliases.optString(i);
            Vulnerability.Reference alias = new Vulnerability.Reference();
            alias.setId(osvAlias);
            Vulnerability.Source aliasSource = new Vulnerability.Source();
            aliasSource.setName(extractSource(osvAlias));
            alias.setSource(aliasSource);
            aliases.add(alias);
        }
        return aliases;
    }

    private List<Vulnerability.Rating> parseCvssRatings(JSONObject object, Vulnerability.Rating.Severity severity) {
        List<Vulnerability.Rating> ratings = new ArrayList<>();
        final JSONArray cvssList = object.optJSONArray("severity");
        if (cvssList != null) {
            for (int i=0; i<cvssList.length(); i++) {

                final JSONObject cvssObj = cvssList.getJSONObject(i);
                String vector = cvssObj.optString("score", null);
                Cvss cvss = Cvss.fromVector(vector);

                Vulnerability.Rating rating = new Vulnerability.Rating();
                final double score = cvss.calculateScore().getBaseScore();
                rating.setVector(vector);
                rating.setScore(score);
                final String type = cvssObj.optString("type", null);
                if (type.equalsIgnoreCase("CVSS_V3")) {
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV3);
                    rating.setSeverity(Vulnerability.Rating.Severity.fromString(
                            String.valueOf(normalizedCvssV3Score(score)).toLowerCase()));
                }
                else {
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV2);
                    rating.setSeverity(Vulnerability.Rating.Severity.fromString(
                            String.valueOf(normalizedCvssV2Score(score)).toLowerCase()));
                }
                ratings.add(rating);
            }
        } else {
            Vulnerability.Rating rating = new Vulnerability.Rating();
            rating.setSeverity(severity);
            ratings.add(rating);
        }
        return ratings;
    }

    private List<Integer> parseCwes(JSONArray osvCweIds) {
        List<Integer> cweIds = new ArrayList<>();
        for (int i=0; i<osvCweIds.length(); i++) {
            cweIds.add(CweResolver.getInstance().parseCweString(osvCweIds.optString(i)));
        }
        return cweIds;
    }

    public String trimSummary(String summary) {

        final int MAX_LEN = 255;
        if(summary != null && summary.length() > 255) {
            return StringUtils.substring(summary, 0, MAX_LEN-2) + "..";
        }
        return summary;
    }

    public String extractSource(String vulnId) {
        final String sourceId = vulnId.split("-")[0];
        return switch (sourceId) {
            case "GHSA" -> GITHUB.name();
            case "CVE" -> NVD.name();
            default -> OSV.name();
        };
    }
}
