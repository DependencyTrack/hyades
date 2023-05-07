package org.hyades.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.hibernate.jpa.QueryHints;
import org.hyades.persistence.model.NotificationLevel;
import org.hyades.persistence.model.NotificationRule;
import org.hyades.persistence.model.NotificationScope;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class NotificationRuleRepository implements PanacheRepository<NotificationRule> {

    public List<NotificationRule> findByScopeAndForLevel(final NotificationScope scope, final NotificationLevel level) {
        return switch (level) {
            case INFORMATIONAL -> find("scope = :scope and notificationLevel = :level",
                    Parameters.with("scope", scope)
                            .and("level", level))
                    .withHint(QueryHints.HINT_READONLY, true)
                    .list();
            case WARNING ->
                    find("scope = :scope and (notificationLevel = :levelWarn or notificationLevel = :levelInfo)",
                            Parameters.with("scope", scope)
                                    .and("levelWarn", level)
                                    .and("levelInfo", NotificationLevel.INFORMATIONAL))
                            .withHint(QueryHints.HINT_READONLY, true)
                            .list();
            case ERROR -> find("""
                            scope = :scope and (
                                notificationLevel = :levelErr or
                                notificationLevel = :levelWarn or
                                notificationLevel = :levelInfo
                            )
                            """,
                    Parameters.with("scope", scope)
                            .and("levelErr", level)
                            .and("levelWarn", NotificationLevel.WARNING)
                            .and("levelInfo", NotificationLevel.INFORMATIONAL))
                    .withHint(QueryHints.HINT_READONLY, true)
                    .list();
        };
    }

}
