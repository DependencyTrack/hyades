package org.acme.osv;

import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.acme.resolver.CweResolver;
import org.apache.commons.lang3.StringUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.vulnerability.Vulnerability;
import us.springett.cvss.Cvss;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.acme.commonutil.JsonUtil.jsonStringToTimestamp;
import static org.acme.model.Vulnerability.Source.GITHUB;
import static org.acme.model.Vulnerability.Source.NVD;
import static org.acme.model.Vulnerability.Source.OSV;

public class OsvToCyclonedxParser {

    public Bom parse(final JSONObject object) {

        Bom cyclonedxBom = null;

        // initial check if advisory is valid or withdrawn
        String withdrawn = object.optString("withdrawn", null);

        if(object != null && withdrawn == null) {

            cyclonedxBom = new Bom();
            Vulnerability vulnerability = new Vulnerability();
            vulnerability.setId(object.optString("id", null));
            vulnerability.setDescription(trimSummary(object.optString("summary", null)));
            vulnerability.setDetail(object.optString("details", null));
            vulnerability.setPublished(Date.from(
                    jsonStringToTimestamp(object.optString("published", null)).toInstant()));
            vulnerability.setUpdated(Date.from(
                    jsonStringToTimestamp(object.optString("modified", null)).toInstant()));

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
                cyclonedxBom.setExternalReferences(externalReferences);
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

            Vulnerability.Rating.Severity severity = Vulnerability.Rating.Severity.UNKNOWN;
            final JSONObject databaseSpecific = object.optJSONObject("database_specific");
            if (databaseSpecific != null) {

                // SEVERITY
                String osvSeverity = databaseSpecific.optString("severity", null);
                if (osvSeverity != null) {
                    severity = Vulnerability.Rating.Severity.valueOf(osvSeverity.toLowerCase());
                }

                // CWEs
                final JSONArray osvCweIds = databaseSpecific.optJSONArray("cwe_ids");
                if(osvCweIds != null) {
                    vulnerability.setCwes(parseCwes(osvCweIds));
                }
            }

            // CVSS ratings
            vulnerability.setRatings(parseCvssRatings(object, severity));

            final JSONArray osvAffectedArray = object.optJSONArray("affected");
            if (osvAffectedArray != null) {

                // CPE
                JSONObject osvAffected = osvAffectedArray.getJSONObject(0);
                final JSONObject cpeObj = osvAffected.optJSONObject("package");
                if (cpeObj != null) {
                    Component component = new Component();
                    component.setName(cpeObj.optString("name", null));
                    component.setPurl(cpeObj.optString("purl", null));
                    cyclonedxBom.setComponents(List.of(component));
                }

                // AFFECTED PACKAGES AND VERSIONS
                vulnerability.setAffects(parseAffectedRanges(osvAffectedArray));
            }
            cyclonedxBom.setVulnerabilities(List.of(vulnerability));
        }

        return cyclonedxBom;
    }

    private List<Vulnerability.Affect> parseAffectedRanges(JSONArray osvAffectedArray) {

        List<Vulnerability.Affect> affects = new ArrayList<>();
        for(int i=0; i<osvAffectedArray.length(); i++) {

            JSONObject osvAffectedObj = osvAffectedArray.getJSONObject(i);
            final JSONArray rangesObj = osvAffectedObj.optJSONArray("ranges");
            final JSONArray versions = osvAffectedObj.optJSONArray("versions");
            Vulnerability.Affect versionRangeAffected = new Vulnerability.Affect();
            List<Vulnerability.Version> versionRanges = new ArrayList<>();

            // TODO: set bom ref for each versionRange

            if (rangesObj != null) {
                for (int j=0; j<rangesObj.length(); j++) {
                    Vulnerability.Version versionRange = new Vulnerability.Version();
                    versionRange.setRange(generateRangeSpecifier(osvAffectedObj, rangesObj.getJSONObject(j), "purlType"));
                    versionRanges.add(versionRange);
                }
            }
            // if ranges are not available or only commit hash range is available, look for versions
            if (versions != null && versions.length() > 0) {
                Vulnerability.Version versionRange = new Vulnerability.Version();
                versionRange.setVersion(generateVersionSpecifier(versions, "purlType"));
                versionRanges.add(versionRange);
            }
            versionRangeAffected.setVersions(versionRanges);
            affects.add(versionRangeAffected);
        }
        return affects;
    }

    private String generateVersionSpecifier(JSONArray versions, String purlType) {

        String version = "vers:" + purlType + "/";
        for (int i=0; i<versions.length(); i++) {
            version += versions.getString(i) + "|";
        }
        return version;
    }

    private String generateRangeSpecifier(JSONObject affectedRange, JSONObject range, String purlType) {

        String uniVersionRange = null;

        final String rangeType = range.optString("type");
        if (!"ECOSYSTEM".equalsIgnoreCase(rangeType) && !"SEMVER".equalsIgnoreCase(rangeType)) {
            // We can't support ranges of type GIT for now, as evaluating them requires knowledge of
            // the entire Git history of a package. We don't have that, so there's no point in
            // ingesting this data.
            //
            // We're also implicitly excluding ranges of types that we don't yet know of.
            // This is a tradeoff of potentially missing new data vs. flooding our users'
            // database with junk data.
            return uniVersionRange;
        }

        final JSONArray rangeEvents = range.optJSONArray("events");
        if (rangeEvents == null) {
            return uniVersionRange;
        }

        for (int i = 0; i < rangeEvents.length(); i++) {
            uniVersionRange += "vers:"+purlType+"/";
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
        }
        return uniVersionRange;
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
                rating.setSeverity(severity);
                rating.setVector(vector);
                rating.setScore(cvss.calculateScore().getBaseScore());
                final String type = cvssObj.optString("type", null);
                if (type.equalsIgnoreCase("CVSS_V3")) {
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV3);
                }
                else {
                    rating.setMethod(Vulnerability.Rating.Method.CVSSV2);
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
