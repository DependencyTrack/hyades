package org.hyades.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.jpa.QueryHints;
import org.hyades.persistence.model.Repository;
import org.hyades.persistence.model.RepositoryType;

import java.util.List;

@ApplicationScoped
public class RepoEntityRepository implements PanacheRepository<Repository> {

    public List<Repository> findEnabledRepositoriesByType(final RepositoryType type) {
        return find("type = :type AND enabled = :enabled ORDER BY resolutionOrder ASC",
                Parameters.with("type", type).and("enabled", true))
                .withHint(QueryHints.HINT_READONLY, true)
                .list();
    }

}
