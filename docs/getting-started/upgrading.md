### Upgrading to 0.7.0

* **Vulnerability policy REST API endpoints have been migrated from v1 to v2.**
  The following v1 endpoints have been removed:

    | Removed endpoint                             | Replacement                           |
    |:---------------------------------------------|:--------------------------------------|
    | `GET /api/v1/policy/vulnerability`           | `GET /api/v2/vuln-policies`           |
    | `GET /api/v1/policy/vulnerability/{uuid}`    | `GET /api/v2/vuln-policies/{uuid}`    |
    | `PUT /api/v1/policy/vulnerability`           | `POST /api/v2/vuln-policies`          |
    | `POST /api/v1/policy/vulnerability`          | `PUT /api/v2/vuln-policies/{uuid}`    |
    | `DELETE /api/v1/policy/vulnerability/{uuid}` | `DELETE /api/v2/vuln-policies/{uuid}` |
    | `GET /api/v1/policy/vulnerability/bundle`    | `GET /api/v2/vuln-policy-bundles`     |

    Vulnerability policies can now be created and managed directly via the REST API,
    without requiring an external bundle. Policies managed via bundles continue to work.

* **Vulnerability policies now support only a single condition per policy.**
  Previously, multiple conditions could be defined per policy. During the upgrade,
  only the first condition of each policy is retained. Policies that relied on multiple
  conditions must be restructured, potentially by splitting them into separate policies
  or by combining conditions using CEL expressions.

* **S3 support for vulnerability policy bundle retrieval has been removed.**
  The following configuration properties are no longer recognized:

    | Removed property                         |
    |:-----------------------------------------|
    | `dt.vulnerability.policy.s3.access.key`  |
    | `dt.vulnerability.policy.s3.secret.key`  |
    | `dt.vulnerability.policy.s3.bucket.name` |
    | `dt.vulnerability.policy.s3.bundle.name` |
    | `dt.vulnerability.policy.s3.region`      |

    Deployments that previously fetched bundles from S3 must switch to serving the bundle
    via HTTP(S) and use `dt.vulnerability.policy.bundle.url` instead.

* **The `dt.vulnerability.policy.bundle.source.type` configuration property has been removed.**
  Bundle retrieval now only supports HTTP(S). No replacement is necessary if `dt.vulnerability.policy.bundle.url`
  is already configured with an HTTP(S) URL.

* **The `dt.vulnerability.policy.analysis.enabled` configuration property has been removed.**
  Vulnerability policy analysis is now always enabled when policies exist.

* The vulnerability policy bundle sync task configuration has been renamed:

    | Before                                                 | After                                           |
    |:-------------------------------------------------------|:------------------------------------------------|
    | `dt.task.vulnerability.policy.fetch.cron`              | `dt.task.vulnerability-policy-bundle-sync.cron` |
    | `dt.task.vulnerability.policy.fetch.lock.max.duration` | *(removed)*                                     |
    | `dt.task.vulnerability.policy.fetch.lock.min.duration` | *(removed)*                                     |

    The default sync interval changed from every 5 minutes to every 15 minutes.

* **Secrets for Fortify SSC, DefectDojo, Kenna Security, and repository passwords have been
  migrated to [secret management](../operations/secret-management/overview.md).** During the
  upgrade, Fortify SSC tokens, DefectDojo API keys, and Kenna Security tokens are cleared from
  the database, and repositories with stored passwords are disabled. After upgrading, configure
  the required secrets via the secret management provider, then manually re-enable the affected
  repositories.

* **Bearer tokens changed from JWTs to opaque tokens.** All existing user sessions
  are invalidated on upgrade. Users will need to log in again. The session timeout configuration
  property has been renamed:

    | Before                     | After                        | Default  |
    |:---------------------------|:-----------------------------|:---------|
    | `dt.auth.jwt.ttl.seconds`  | `dt.auth.session-timeout-ms` | 28800000 |

    Note that the default session lifetime changed from 7 days to 8 hours. Deployments that
    relied on the previous default, or that configured `dt.auth.jwt.ttl.seconds` explicitly,
    must update their configuration accordingly.

* **The role system introduced in 5.6.0 has been removed**.
  It [never worked as intended](https://github.com/DependencyTrack/hyades/issues/2116),
  weakened the existing authorization mechanism, and fixing it is not possible without 
  significant breaking changes to the system.
* The minimum supported PostgreSQL version has been raised from 13 to 14 ([hyades/#1910]).
* **Kafka is no longer required**. The platform can now sustain itself on PostgreSQL alone
  and no longer requires a separate message broker. Most of the asynchronous processing has
  been migrated to a [durable execution](../architecture/design/durable-execution.md) model.
* **The vulnerability-analyzer service has been removed**. Vulnerability analysis
  is now performed by the apiserver. Refer to the [design documentation](../architecture/design/vulnerability-analysis.md) for details.
* **The repository-meta-analyzer service has been removed**. Package metadata resolution
  is now performed by the apiserver. Refer to the [design documentation](../architecture/design/package-metadata-resolution.md) for details.
* **The notification-publisher service has been removed**. Publishing of notifications
  is now performed by the apiserver. Refer to the [design documentation](../architecture/design/notifications.md) for details.
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

* **All configuration properties have been standardized to use the `dt.` prefix**.                                                                                
  Properties that previously used the `alpine.` prefix, or no prefix at all, are now under `dt.`.
  For example:

    | Before                               | After                                   |
    |--------------------------------------|-----------------------------------------|
    | `alpine.ldap.enabled`                | `dt.ldap.enabled`                       |
    | `alpine.oidc.enabled`                | `dt.oidc.enabled`                       |
    | `task.portfolio.metrics.update.cron` | `dt.task.portfolio.metrics.update.cron` |

    * The naming rule is straightforward:
        * `alpine.<suffix>` → `dt.<suffix>` (for all former `alpine.*` properties)
        * `<name>` → `dt.<name>` (for all previously unprefixed properties)

    When using environment variables, the same mapping applies: `ALPINE_LDAP_ENABLED` becomes
    `DT_LDAP_ENABLED`, `TASK_PORTFOLIO_METRICS_UPDATE_CRON` becomes `DT_TASK_PORTFOLIO_METRICS_UPDATE_CRON`,
    and so on.

    No immediate action is required. Old property names and environment variables continue to work
    via a fallback mechanism. However, they are deprecated and support will be removed in a future release.
    Users are encouraged to update their configurations at their earliest convenience.

* **Health and metrics endpoints have moved to a dedicated management server** that listens
  on a separate port (default `9000`). The management server starts before init tasks, making
  health probes available during the entire startup sequence.

    | Endpoint  | Before                              | After                               |
    |:----------|:------------------------------------|:------------------------------------|
    | Health    | `http://<host>:8080/health`         | `http://<host>:9000/health`         |
    | Liveness  | `http://<host>:8080/health/live`    | `http://<host>:9000/health/live`    |
    | Readiness | `http://<host>:8080/health/ready`   | `http://<host>:9000/health/ready`   |
    | Startup   | `http://<host>:8080/health/started` | `http://<host>:9000/health/started` |
    | Metrics   | `http://<host>:8080/metrics`        | `http://<host>:9000/metrics`        |

    Refer to the [observability documentation](../operations/observability.md) for details.

* **The `LOGGING_LEVEL` environment variable has been removed**. Log levels are now configured
  via `dt.logging.level."<logger-name>"` per-logger properties.
  Refer to the [logging documentation](../operations/observability.md#logging) for details.
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
