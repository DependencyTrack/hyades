# Database Migrations

## Introduction

In contrast to Dependency-Track v4 and earlier, Dependency-Track v5 manages database migrations with [Liquibase].
The database schema is still *owned* by the API server though. It will execute migrations upon startup,
unless explicitly disabled via [`database.run.migrations`](../../reference/configuration/api-server/#databaserunmigrations).

[Liquibase] operates with the concept of [changelogs](https://docs.liquibase.com/concepts/changelogs/home.html).
For the sake of better visibility, Dependency-Track uses separate changelogs for each release version.
Individual changelogs are referenced by [`changelog-main.xml`](https://github.com/DependencyTrack/hyades-apiserver/blob/main/src/main/resources/migration/changelog-main.xml).

Stored procedures and custom SQL functions are treated differently: They are re-created whenever their
content changes. Their sources are located in the [`procedures`](https://github.com/DependencyTrack/hyades-apiserver/tree/main/src/main/resources/migration/procedures) directory.

## Adding Migrations

1. If it doesn't exist already, create a `changelog-vX.Y.Z.xml` file
    * `X`, `Y`, and `Z` must correspond to the current release version
2. Ensure the `changelog-vX.Y.Z.xml` file is referenced via `include` in `changelog-main.xml`
3. Add your [changeset](https://docs.liquibase.com/concepts/changelogs/changeset.html) to `changelog-vX.Y.Z.xml`

When adding a new `changeset`, consider the following guidelines:

* The changeset ID **must** follow the `vX.Y.Z-<NUM>` format, where:
    * `X`, `Y`, and `Z` match the changelog's version
    * `NUM` is an incrementing number, starting at `1` for the first `changeset` of the release
* The `author` **must** correspond to your GitHub username
* Prefer [built-in change types](https://docs.liquibase.com/change-types/home.html)
    * Use the [`sql` change type](https://docs.liquibase.com/change-types/sql.html) if no fitting built-in exists
    * Use a [custom change](https://docs.liquibase.com/change-types/custom-change.html) *in edge cases*, when additional computation is required
* When using custom changes:
    * Use the [`org.dependencytrack.persistence.migration.change`](https://github.com/DependencyTrack/hyades-apiserver/tree/main/src/main/java/org/dependencytrack/persistence/migration/change) package
    * Changes **must not** depend on domain logic
* You **must not** modify `changeset`s that were already committed to `main`

## Making Schema Changes Available to Hyades Services

Because the schema is owned by the API server, and the API server is also responsible for executing migrations,
other services that access the database must replicate the current schema, in order to run tests against it.

Currently, this is achieved by:

1. Having [Liquibase] generate the schema SQL based on the changelog
2. Adding the `schema.sql` file as resource to the [`commons-persistence`](https://github.com/DependencyTrack/hyades/blob/c1463a80c8ad45e0ef401292cf29dbd37a7de308/commons-persistence/src/main/resources/schema.sql) module
3. Having all services that require database access depend on `commons-persistence`
4. Configuring [Quarkus Dev Services](https://quarkus.io/guides/databases-dev-services) to initialize new database containers with `schema.sql`
    * Using [`quarkus.datasource.devservices.init-script-path`](https://quarkus.io/guides/datasource#quarkus-datasource_quarkus-datasource-devservices-init-script-path)

The schema can be generated using the [`dbschema-generate.sh`](https://github.com/DependencyTrack/hyades-apiserver/blob/794e9eaa2991f961223653b293c46ce64bc7e0ce/dev/scripts/dbschema-generate.sh)
script in the `hyades-apiserver` repository:

```shell
./dev/scripts/dbschema-generate.sh
```

!!! note
    * You may need to [build](building.md) the API server project once before running the script
    * Because Liquibase requires database to run against, the script will launch a temporary PostgreSQL container

The output is written to `target/liquibase/migrate.sql`.

[Liquibase]: https://www.liquibase.com/