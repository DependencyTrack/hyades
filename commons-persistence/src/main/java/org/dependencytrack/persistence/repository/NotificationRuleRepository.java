package org.dependencytrack.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.dependencytrack.persistence.model.NotificationLevel;
import org.dependencytrack.persistence.model.NotificationRule;
import org.dependencytrack.persistence.model.NotificationScope;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@ApplicationScoped
public class NotificationRuleRepository implements PanacheRepository<NotificationRule> {

    public List<NotificationRule> findEnabledByScopeAndForLevel(final NotificationScope scope, final NotificationLevel level) {
        return switch (level) {
            case INFORMATIONAL -> find("enabled and scope = :scope and notificationLevel = :level",
                    Parameters.with("scope", scope)
                            .and("level", level))
                    .withHint(HINT_READ_ONLY, true)
                    .list();
            case WARNING ->
                    find("enabled and scope = :scope and (notificationLevel = :levelWarn or notificationLevel = :levelInfo)",
                            Parameters.with("scope", scope)
                                    .and("levelWarn", level)
                                    .and("levelInfo", NotificationLevel.INFORMATIONAL))
                            .withHint(HINT_READ_ONLY, true)
                            .list();
            case ERROR -> find("""
                           enabled and scope = :scope and (
                                notificationLevel = :levelErr or
                                notificationLevel = :levelWarn or
                                notificationLevel = :levelInfo
                            )
                            """,
                    Parameters.with("scope", scope)
                            .and("levelErr", level)
                            .and("levelWarn", NotificationLevel.WARNING)
                            .and("levelInfo", NotificationLevel.INFORMATIONAL))
                    .withHint(HINT_READ_ONLY, true)
                    .list();
        };
    }

}
