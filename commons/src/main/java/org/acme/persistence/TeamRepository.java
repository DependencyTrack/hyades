package org.acme.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.model.Team;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@ApplicationScoped
public class TeamRepository implements PanacheRepository<Team> {

    private final EntityManager entityManager;

    @Inject
    TeamRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public List<Team> findByNotificationRule(final long notificationRuleId) {
        final Query query = entityManager.createNativeQuery("""
                SELECT * FROM "TEAM" AS "T"
                    INNER JOIN "NOTIFICATIONRULE_TEAMS" AS "NT" ON "NT"."TEAM_ID" = "T"."ID"
                WHERE "NT"."NOTIFICATIONRULE_ID" = :ruleId  
                """, Team.class);
        query.setParameter("ruleId", notificationRuleId);
        return query.getResultList();
    }

}
