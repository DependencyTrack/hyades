package org.dependencytrack.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.jpa.QueryHints;
import org.dependencytrack.persistence.model.Team;

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
        return entityManager
                .createNativeQuery("""
                        SELECT * FROM "TEAM" AS "T"
                            INNER JOIN "NOTIFICATIONRULE_TEAMS" AS "NT" ON "NT"."TEAM_ID" = "T"."ID"
                        WHERE "NT"."NOTIFICATIONRULE_ID" = :ruleId  
                        """, Team.class)
                .setParameter("ruleId", notificationRuleId)
                .setHint(QueryHints.HINT_READONLY, true)
                .getResultList();
    }

}
