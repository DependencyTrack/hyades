package org.hyades.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.hibernate.jpa.QueryHints;
import org.hyades.model.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConfigPropertyRepository implements PanacheRepository<ConfigProperty> {

    public ConfigProperty findByGroupAndName(String group, String propertyName) {
        return find("groupName = :group and propertyName = :property",
                Parameters.with("group", group)
                        .and("property", propertyName))
                .withHint(QueryHints.HINT_READONLY, true)
                .firstResult();
    }

}
