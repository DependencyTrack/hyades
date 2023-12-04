package org.dependencytrack.persistence.repository;

import com.github.packageurl.PackageURL;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.dependencytrack.persistence.model.VulnerableSoftware;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class VulnerableSoftwareRepository implements PanacheRepository<VulnerableSoftware> {

    public List<VulnerableSoftware> getAllVulnerableSoftware(final String cpePart, final String cpeVendor,
                                                              final String cpeProduct, final PackageURL purl) {
        var queryFilterParts = new ArrayList<String>();
        var queryParams = new HashMap<String, Object>();

        if (cpePart != null && cpeVendor != null && cpeProduct != null) {
            final var cpeQueryFilterParts = new ArrayList<String>();

            // The query composition below represents a partial implementation of the CPE
            // matching logic. It makes references to table 6-2 of the CPE name matching
            // specification: https://nvlpubs.nist.gov/nistpubs/Legacy/IR/nistir7696.pdf
            //
            // In CPE matching terms, the parameters of this method represent the target,
            // and the `VulnerableSoftware`s in the database represent the source.
            //
            // While the source *can* contain wildcards ("*", "?"), there is currently (Oct. 2023)
            // no occurrence of part, vendor, or product with wildcards in the NVD database.
            // Evaluating wildcards in the source can only be done in-memory. If we wanted to do that,
            // we'd have to fetch *all* records, which is not practical.

            if (!"*".equals(cpePart) && !"-".equals(cpePart)) {
                // | No. | Source A-V      | Target A-V | Relation             |
                // | :-- | :-------------- | :--------- | :------------------- |
                // | 3   | ANY             | i          | SUPERSET             |
                // | 7   | NA              | i          | DISJOINT             |
                // | 9   | i               | i          | EQUAL                |
                // | 10  | i               | k          | DISJOINT             |
                // | 14  | m1 + wild cards | m2         | SUPERSET or DISJOINT |
                // TODO: Filter should use equalsIgnoreCase as CPE matching is case-insensitive.
                //   Can't currently do this as it would require an index on UPPER("PART"),
                //   which we cannot add through JDO annotations.
                cpeQueryFilterParts.add("(part = '*' or part = :part)");
                queryParams.put("part", cpePart);

                // NOTE: Target *could* include wildcard, but the relation
                // for those cases is undefined:
                //
                // | No. | Source A-V      | Target A-V      | Relation   |
                // | :-- | :-------------- | :-------------- | :--------- |
                // | 4   | ANY             | m + wild cards  | undefined  |
                // | 8   | NA              | m + wild cards  | undefined  |
                // | 11  | i               | m + wild cards  | undefined  |
                // | 17  | m1 + wild cards | m2 + wild cards | undefined  |
            } else if ("-".equals(cpePart)) {
                // | No. | Source A-V     | Target A-V | Relation |
                // | :-- | :------------- | :--------- | :------- |
                // | 2   | ANY            | NA         | SUPERSET |
                // | 6   | NA             | NA         | EQUAL    |
                // | 12  | i              | NA         | DISJOINT |
                // | 16  | m + wild cards | NA         | DISJOINT |
                cpeQueryFilterParts.add("(part = '*' or part = '-')");
            } else {
                // | No. | Source A-V     | Target A-V | Relation |
                // | :-- | :------------- | :--------- | :------- |
                // | 1   | ANY            | ANY        | EQUAL    |
                // | 5   | NA             | ANY        | SUBSET   |
                // | 13  | i              | ANY        | SUPERSET |
                // | 15  | m + wild cards | ANY        | SUPERSET |
                cpeQueryFilterParts.add("part is not null");
            }

            if (!"*".equals(cpeVendor) && !"-".equals(cpeVendor)) {
                // TODO: Filter should use equalsIgnoreCase as CPE matching is case-insensitive.
                //   Can't currently do this as it would require an index on UPPER("VENDOR"),
                //   which we cannot add through JDO annotations.
                cpeQueryFilterParts.add("(vendor = '*' or vendor = :vendor)");
                queryParams.put("vendor", cpeVendor);
            } else if ("-".equals(cpeVendor)) {
                cpeQueryFilterParts.add("(vendor = '*' or vendor = '-')");
            } else {
                cpeQueryFilterParts.add("vendor is not null");
            }

            if (!"*".equals(cpeProduct) && !"-".equals(cpeProduct)) {
                // TODO: Filter should use equalsIgnoreCase as CPE matching is case-insensitive.
                //   Can't currently do this as it would require an index on UPPER("PRODUCT"),
                //   which we cannot add through JDO annotations.
                cpeQueryFilterParts.add("(product = '*' or product = :product)");
                queryParams.put("product", cpeProduct);
            } else if ("-".equals(cpeProduct)) {
                cpeQueryFilterParts.add("(product = '*' or product = '-')");
            } else {
                cpeQueryFilterParts.add("product is not null");
            }

            queryFilterParts.add("(%s)".formatted(String.join(" and ", cpeQueryFilterParts)));
        }

        if (purl != null) {
            final var purlFilterParts = new ArrayList<String>();

            if (purl.getType() != null) {
                purlFilterParts.add("purlType = :purlType");
                queryParams.put("purlType", purl.getType());
            } else {
                purlFilterParts.add("purlType is null");
            }

            if (purl.getNamespace() != null) {
                purlFilterParts.add("purlNamespace = :purlNamespace");
                queryParams.put("purlNamespace", purl.getNamespace());
            } else {
                purlFilterParts.add("purlNamespace is null");
            }

            if (purl.getName() != null) {
                purlFilterParts.add("purlName = :purlName");
                queryParams.put("purlName", purl.getName());
            } else {
                purlFilterParts.add("purlName is null");
            }

            queryFilterParts.add("(%s)".formatted(String.join(" and ", purlFilterParts)));
        }

        if (queryFilterParts.isEmpty()) {
            return emptyList();
        }

        return find(String.join(" or ", queryFilterParts), queryParams)
                .withHint(HINT_READ_ONLY, true)
                .list();
    }

}
