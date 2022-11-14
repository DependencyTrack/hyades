package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.acme.model.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class ConfigPropertyRepository implements PanacheRepository<ConfigProperty> {
    public ConfigProperty findByGroupAndName(String group, String propertyName) {
        return list("groupName = :group and propertyName = :property",
                        Parameters.with("group", group)
                                .and("property", propertyName)).get(0);
    }
}
