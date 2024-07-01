## New Features

<!-- TODO: Add more! -->

* New *powerful [CEL]-based policy engine*, providing more flexibility while being more efficient
than the engine shipped with v4. <!-- TODO: Link to policy docs when ready! -->
* Ability to automatically audit vulnerabilities across the entire portfolio using [CEL] expressions. <!-- TODO: Link to docs when ready! -->
* Hash-based *integrity analysis* for components. <!-- TODO: Link to integrity analysis docs when ready! -->
* The API server now supports *high availability (HA) deployments* in active-active configuration.
* *Zero downtime deployments* when running API server in HA configuration.
* *Greatly reduced resource footprint* of the API server.
* The status of asynchronous tasks (e.g. vulnerability analysis) is now
  [tracked in a persistent manner](../architecture/design/workflow-state-tracking.md),
  improving observability.

## Architecture / Operations

<!-- TODO: Add more! -->

* [PostgreSQL] is the only [supported](../operations/database.md) database.
    * Support for H2, MySQL, and Microsoft SQL Server is dropped.
* To facilitate communication between services, a [Kafka]-compatible broker is required.
* Publishing of notifications, fetching component metadata from repositories,
and vulnerability analysis is performed by services separately from the API server.
    * The services can be scaled up and down as needed.
    * Some services (i.e. `notification-publisher`) can be omitted entirely from a deployment,
      if publishing of notification via e.g. Webhook is not needed.
* All services **except the API server** can optionally be deployed as native executables
(thanks to [GraalVM]), offering a lower resource footprint than their JVM-based counterparts.
* [Database migrations] are performed through a more reliable, changelog-based approach.

## Breaking Changes

### Notifications

* `subject` objects passed to notification templates are now objects generated from [Protobuf] definitions.
    * The respective schema is defined in [notification.proto].
    * List fields now have a `List` suffix (i.e. `vulnerabilities` -> `vulnerabilitiesList`).
* Level values are now prefixed with `LEVEL_`
    * Before: `INFORMATIONAL`
    * Now: `LEVEL_INFORMATIONAL`
* Scope values are now prefixed with `SCOPE_`
    * Before: `SYSTEM`
    * Now: `SCOPE_SYSTEM`
* Group values are now prefixed with `GROUP_`
    * Before: `NEW_VULNERABILITY`
    * Now: `GROUP_NEW_VULNERABILITY`
* The `timestamp` value passed to notification templates is now consistently formatted with three fractional digits.
    * Before, any of:
        * `1970-01-01T00:11:06Z`
        * `1970-01-01T00:11:06.000Z`
        * `1970-01-01T00:11:06.000000Z`
        * `1970-01-01T00:11:06.000000000Z`
    * Now: `1970-01-01T00:11:06.000Z`

### Search

* The API server no longer maintains [Lucene] indexes.
    * The local `~/.dependency-track/index` directory is no longer required.
* All REST endpoints under `/api/v1/search` were removed.
* Fuzzy matching for the internal analyzer is no longer supported.

[notification.proto]: https://github.com/DependencyTrack/hyades/blob/main/proto/src/main/proto/org/dependencytrack/notification/v1/notification.proto
[CEL]: https://cel.dev/
[Database migrations]: ../development/database-migrations.md
[GraalVM]: https://www.graalvm.org/
[Kafka]: https://kafka.apache.org/
[Lucene]: https://lucene.apache.org/
[PostgreSQL]: https://www.postgresql.org/
[Protobuf]: https://protobuf.dev/