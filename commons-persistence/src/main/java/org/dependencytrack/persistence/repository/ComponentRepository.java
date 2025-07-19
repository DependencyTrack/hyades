package org.dependencytrack.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.dependencytrack.persistence.model.Component;

@ApplicationScoped
public class ComponentRepository implements PanacheRepository<Component> {

}
