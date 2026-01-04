### Upgrading to 0.7.0

* The minimum supported PostgreSQL version has been raised from 13 to 14 ([hyades/#1910]).
* **The notification-publisher service has been removed**. Publishing of notifications
  is now performed by the apiserver.
* The way notification publishers and alerts are configured behind the scenes has changed.
    * Existing configuration is migrated during the upgrade on a best-effort basis.
    * To prevent partially migrated alert configurations from taking effect,
      **all alerts are disabled during the upgrade**.
    * You must manually review and re-enable them after the upgrade.
    * Refer to the [notification publishers documentation](../usage/notifications/publishers.md) for guidance.
* **Notifications are no longer published to Kafka by default**. Going forward, you must configure
  alerts explicitly, and use the new [Kafka publisher](../usage/notifications/publishers.md#kafka)
  if you want to receive notifications via Kafka.
* Email server configuration in the UI has moved from `Administration → General → Email` to
  `Administration → Notifications → Publishers → Email`. **Previously configured email settings
  are discarded** during the upgrade, and you'll need to reconfigure it if you rely on notifications
  being sent via email.
* Various database configurations in the API server have been deprecated:

    | Before                              | After                                |
    |:------------------------------------|:-------------------------------------|
    | `alpine.database.url`               | `dt.datasource.url`                  |
    | `alpine.database.username`          | `dt.datasource.username`             |
    | `alpine.database.password`          | `dt.datasource.password`             |
    | `alpine.database.password.file`     | `dt.datasource.password-file`        |
    | `alpine.database.pool.enabled`      | `dt.datasource.pool.enabled`         |
    | `alpine.database.pool.max.size`     | `dt.datasource.pool.max-size`        |
    | `alpine.database.pool.min.idle`     | `dt.datasource.pool.min-idle`        |
    | `alpine.database.pool.idle.timeout` | `dt.datasource.pool.idle-timeout-ms` |
    | `alpine.database.pool.max.lifetime` | `dt.datasource.pool.max-lifetime-ms` |

    * For this version, the `dt.datasource.*` configurations default to their `alpine.database.*`
      counterparts. Existing deployments should continue to function without changes.
      However, support for `alpine.database.*` configs will be removed prior to the GA release.
    * The new datasource configuration mechanism is documented [here](../operations/configuration/datasources.md).  

* The following init task configurations have been removed and replaced with `init.tasks.datasource.name`:
    * `init.tasks.database.url`
    * `init.tasks.database.username`
    * `init.tasks.database.password`
* Refer to the [schema migrations](../operations/database.md#schema-migrations) documentation
  for an example of how to run init tasks with separate database credentials. 

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

* Database migrations have been integrated into the broader concept of *initialization tasks*.  
Consequently, the following configuration properties were renamed:

    | Before                         | After                          |
    |:-------------------------------|:-------------------------------|
    | `database.run.migrations`      | `init.tasks.enabled`           |
    | `database.run.migrations.only` | `init.and.exit`                |
    | `database.migration.url`       | `init.tasks.database.url`      |
    | `database.migration.username`  | `init.tasks.database.username` |
    | `database.migration.password`  | `init.tasks.database.password` |

[apiserver/#840]: https://github.com/DependencyTrack/hyades-apiserver/pull/840
[apiserver/#888]: https://github.com/DependencyTrack/hyades-apiserver/pull/888
[apiserver/#904]: https://github.com/DependencyTrack/hyades-apiserver/pull/904
[apiserver/#910]: https://github.com/DependencyTrack/hyades-apiserver/pull/910
[apiserver/#1141]: https://github.com/DependencyTrack/hyades-apiserver/pull/1141
[apiserver/#1169]: https://github.com/DependencyTrack/hyades-apiserver/pull/1169
[hyades/#1392]: https://github.com/DependencyTrack/hyades/issues/1392
[hyades/#1724]: https://github.com/DependencyTrack/hyades/issues/1724
[hyades/#1910]: https://github.com/DependencyTrack/hyades/issues/1910

[kafka configuration reference]: ../reference/configuration/api-server.md#kafka
[task scheduling configuration reference]: ../reference/configuration/api-server.md#task-scheduling
