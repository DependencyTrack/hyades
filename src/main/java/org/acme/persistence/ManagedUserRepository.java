package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.model.ManagedUser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@ApplicationScoped
public class ManagedUserRepository implements PanacheRepository<ManagedUser> {

    private final EntityManager entityManager;

    @Inject
    ManagedUserRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<String> findEmailsByTeam(final long teamId) {
        final Query query = entityManager.createNativeQuery("""
                SELECT "MU"."EMAIL" FROM "MANAGEDUSER" AS "MU"
                    INNER JOIN "MANAGEDUSERS_TEAMS" AS "MUT" ON "MUT"."MANAGEDUSER_ID" = "MU"."ID"
                WHERE "MUT"."TEAM_ID" = :teamId AND "MU"."EMAIL" IS NOT NULL
                """);
        query.setParameter("teamId", teamId);
        return query.getResultList();
    }

}
