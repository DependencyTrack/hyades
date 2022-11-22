package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.acme.model.Repository;
import org.acme.model.RepositoryType;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RepoEntityRepository implements PanacheRepository<Repository> {

    public List<Repository> findRepositoryByRepositoryType(RepositoryType type){
        return list("type = :type order by resolutionOrder asc",
                Parameters.with("type", type));
    }

}
