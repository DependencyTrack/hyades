package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.acme.model.Component;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ComponentRepository implements PanacheRepository<Component> {
    public Component getComponentByUUID(UUID uuid){
        return list("uuid = :uuid",
                Parameters.with("uuid", uuid)).get(0);
    }
}
