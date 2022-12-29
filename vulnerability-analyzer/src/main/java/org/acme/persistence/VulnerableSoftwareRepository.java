package org.acme.persistence;

import com.github.packageurl.PackageURL;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.acme.model.VulnerableSoftware;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class VulnerableSoftwareRepository implements PanacheRepository<VulnerableSoftware> {

    public List<VulnerableSoftware> getAllVulnerableSoftware(final String part, final String vendor, final String product, final PackageURL purl) {
        boolean cpeSpecified = (part != null && vendor != null && product != null);
        if (cpeSpecified && purl != null) {
            return list("part = :part and vendor = :vendor and product = :product or (purlType = :purlType and purlNamespace = :purlNamespace and purlName = :purlName)",
                    Parameters.with("part", part)
                            .and("vendor", vendor)
                            .and("product", product)
                            .and("purlType", purl.getType())
                            .and("purlNamespace", purl.getNamespace())
                            .and("purlName", purl.getName()));
        }
        else if (cpeSpecified) {
            return list("part = :part and vendor = :vendor and product = :product",
                    Parameters.with("part", part)
                            .and("vendor", vendor)
                            .and("product", product));
        }
        else if (purl != null) {
            return list("purlType = :purlType and purlNamespace = :purlNamespace and purlName = :purlName",
                    Parameters.with("purlType", purl.getType())
                            .and("purlNamespace", purl.getNamespace())
                            .and("purlName", purl.getName()));
        }
        else {
            return new ArrayList<>();
        }
    }
}
