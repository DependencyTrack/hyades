package org.dependencytrack.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.dependencytrack.persistence.model.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class ConfigPropertyRepository implements PanacheRepository<ConfigProperty> {

    public ConfigProperty findByGroupAndName(final String group, final String propertyName) {
        return find("groupName = :group and propertyName = :property",
                Parameters.with("group", group)
                        .and("property", propertyName))
                .withHint(HINT_READ_ONLY, true)
                .firstResult();
    }

}
