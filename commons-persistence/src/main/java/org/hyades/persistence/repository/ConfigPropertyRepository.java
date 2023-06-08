package org.hyades.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.jpa.QueryHints;
import org.hyades.persistence.model.ConfigProperty;

@ApplicationScoped
public class ConfigPropertyRepository implements PanacheRepository<ConfigProperty> {

    public ConfigProperty findByGroupAndName(final String group, final String propertyName) {
        return find("groupName = :group and propertyName = :property",
                Parameters.with("group", group)
                        .and("property", propertyName))
                .withHint(QueryHints.HINT_READONLY, true)
                .firstResult();
    }

}
