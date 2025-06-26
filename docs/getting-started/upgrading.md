### Upgrading to 0.6.0

* The `kafka.topic.prefix` configuration was renamed to `dt.kafka.topic.prefix` to prevent
collisions with native Kafka properties ([hyades/#1392]).
* Configuration names for task cron expressions and lock durations have changed ([apiserver/#840]).
They now follow a consistent `task.<task-name>.<config>` scheme. Lock durations are now specified
in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601#Durations) format instead of milliseconds.
Refer to the [task scheduling configuration reference] for details. Example of name change:

    | Before                                          | After                                             |
    |:------------------------------------------------|:--------------------------------------------------|
    | `task.cron.metrics.portfolio`                   | `task.portfolio.metrics.update.cron`              |
    | `task.metrics.portfolio.lockAtMostForInMillis`  | `task.portfolio.metrics.update.lock.max.duration` |
    | `task.metrics.portfolio.lockAtLeastForInMillis` | `task.portfolio.metrics.update.lock.min.duration` |

* The `/api/v1/vulnerability/source/{source}/vuln/{vuln}/projects` REST API endpoint now supports pagination
([apiserver/#888]). Like all other paginated endpoints, the page size defaults to `100`.
Clients currently expecting *all* items to be returned at once must be updated to deal with pagination.
* The `alpine.` prefix was removed from Kafka processor properties of the API server ([apiserver/#904]).
Refer to the [kafka configuration reference] for details. Example of name change:

    | Before                                                     | After                                               |
    |:-----------------------------------------------------------|:----------------------------------------------------|
    | `alpine.kafka.processor.vuln.scan.result.processing.order` | `kafka.processor.vuln.scan.result.processing.order` |

* The endpoints deprecated in v4.x mentioned below were removed ([apiserver/#910]):

    | Removed endpoint                                   | Replacement                        |
    |:---------------------------------------------------|:-----------------------------------|
    | `POST /api/v1/policy/{policyUuid}/tag/{tagName}`   | `POST /api/v1/tag/{name}/policy`   |
    | `DELETE /api/v1/policy/{policyUuid}/tag/{tagName}` | `DELETE /api/v1/tag/{name}/policy` |
    | `GET /api/v1/tag/{policyUuid}`                     | `GET /api/v1/tag/policy/{uuid}`    |
    | `GET /api/v1/bom/token/{uuid}`                     | `GET /api/v1/event/token/{uuid}`   |

* The minimum supported PostgreSQL version has been raised from 11 to 13 ([hyades/#1724]).
  Lower versions may still work, but are no longer tested against.

* User records in the database are consolidated from the separate `LDAPUSER`, `MANAGEDUSER`,
and `OIDCUSER` tables, into a single `USER` table ([apiserver/#1169]). The new `USER` table
enforces uniqueness of usernames. To prevent data loss, `LDAPUSER` and `OIDCUSER` records with
conflicting usernames will have their username values suffixed with `-CONFLICT-LDAP` and
`-CONFLICT-OIDC` respectively. Affected users will not be able to authenticate.
Administrators are expected to resolve this by removing users or renaming them as desired.
Note that this is an edge case and should not affect the vast majority of deployments.

* The metrics tables `DEPENDENCYMETRICS`, `PORTFOLIOMETRICS`, and `PROJECTMETRICS` are
partitioned by date ([apiserver/#1141]). The migration procedure involves copying existing 
metrics data, thus requiring up to double the amount of storage for the duration of the migration.
To reduce the amount of data being copied, consider temporarily reducing the metrics
retention timespan in the administration panel under *Configuration* → *Maintenance*.
Only historic data falling within the configured retention duration will be migrated.

[apiserver/#840]: https://github.com/DependencyTrack/hyades-apiserver/pull/840
[apiserver/#888]: https://github.com/DependencyTrack/hyades-apiserver/pull/888
[apiserver/#904]: https://github.com/DependencyTrack/hyades-apiserver/pull/904
[apiserver/#910]: https://github.com/DependencyTrack/hyades-apiserver/pull/910
[apiserver/#1141]: https://github.com/DependencyTrack/hyades-apiserver/pull/1141
[apiserver/#1169]: https://github.com/DependencyTrack/hyades-apiserver/pull/1169
[hyades/#1392]: https://github.com/DependencyTrack/hyades/issues/1392
[hyades/#1724]: https://github.com/DependencyTrack/hyades/issues/1724

[kafka configuration reference]: ../reference/configuration/api-server.md#kafka
[task scheduling configuration reference]: ../reference/configuration/api-server.md#task-scheduling
