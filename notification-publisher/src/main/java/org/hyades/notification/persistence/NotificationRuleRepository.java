package org.hyades.notification.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import org.hyades.notification.model.NotificationLevel;
import org.hyades.notification.model.NotificationRule;
import org.hyades.notification.model.NotificationScope;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class NotificationRuleRepository implements PanacheRepository<NotificationRule> {

    public List<NotificationRule> findByScopeAndForLevel(final NotificationScope scope, final NotificationLevel level) {
        return switch (level) {
            case INFORMATIONAL -> list("scope = :scope and notificationLevel = :level",
                    Parameters.with("scope", scope)
                            .and("level", level));
            case WARNING ->
                    list("scope = :scope and (notificationLevel = :levelWarn or notificationLevel = :levelInfo)",
                            Parameters.with("scope", scope)
                                    .and("levelWarn", level)
                                    .and("levelInfo", NotificationLevel.INFORMATIONAL));
            case ERROR -> list("""
                            scope = :scope and (
                                notificationLevel = :levelErr or
                                notificationLevel = :levelWarn or
                                notificationLevel = :levelInfo
                            )
                            """,
                    Parameters.with("scope", scope)
                            .and("levelErr", level)
                            .and("levelWarn", NotificationLevel.WARNING)
                            .and("levelInfo", NotificationLevel.INFORMATIONAL));
        };
    }

}
