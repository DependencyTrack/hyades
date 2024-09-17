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

* All deprecated endpoints mentioned below were removed:
    * POST `/api/v1/policy/{policyUuid}/tag/{tagName}`
    * DELETE `/api/v1/policy/{policyUuid}/tag/{tagName}`
    * GET `/api/v1/tag/{policyUuid}`
    * GET `/api/v1/bom/token/{uuid}`

[apiserver/#840]: https://github.com/DependencyTrack/hyades-apiserver/pull/840
[apiserver/#888]: https://github.com/DependencyTrack/hyades-apiserver/pull/888
[apiserver/#904]: https://github.com/DependencyTrack/hyades-apiserver/pull/904
[hyades/#1392]: https://github.com/DependencyTrack/hyades/issues/1392

[kafka configuration reference]: ../reference/configuration/api-server.md#kafka
[task scheduling configuration reference]: ../reference/configuration/api-server.md#task-scheduling
