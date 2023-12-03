package org.dependencytrack.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class UserRepository {

    private final EntityManager entityManager;

    @Inject
    UserRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<String> findEmailsByTeam(final long teamId) {
        return entityManager.createNativeQuery("""
                        SELECT "MU"."EMAIL" AS "EMAIL" FROM "MANAGEDUSER" AS "MU"
                            INNER JOIN "MANAGEDUSERS_TEAMS" AS "MUT" ON "MUT"."MANAGEDUSER_ID" = "MU"."ID"
                        WHERE "MUT"."TEAM_ID" = :teamId AND "MU"."EMAIL" IS NOT NULL
                        UNION
                        SELECT "LU"."EMAIL" AS "EMAIL" FROM "LDAPUSER" AS "LU"
                            INNER JOIN "LDAPUSERS_TEAMS" AS "LUT" ON "LUT"."LDAPUSER_ID" = "LU"."ID"
                        WHERE "LUT"."TEAM_ID" = :teamId AND "LU"."EMAIL" IS NOT NULL
                        UNION 
                        SELECT "OU"."EMAIL" AS "EMAIL" FROM "OIDCUSER" AS "OU"
                            INNER JOIN "OIDCUSERS_TEAMS" AS "OUT" ON "OUT"."OIDCUSERS_ID" = "OU"."ID"
                        WHERE "OUT"."TEAM_ID" = :teamId AND "OU"."EMAIL" IS NOT NULL
                        """)
                .setParameter("teamId", teamId)
                .setHint(HINT_READ_ONLY, true)
                .getResultList();
    }

}
