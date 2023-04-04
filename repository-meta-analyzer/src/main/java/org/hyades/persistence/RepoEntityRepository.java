package org.hyades.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.hibernate.jpa.QueryHints;
import org.hyades.model.Repository;
import org.hyades.model.RepositoryType;

import javax.enterprise.context.ApplicationScoped;
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
