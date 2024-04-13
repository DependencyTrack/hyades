Dependency-Track requires a [PostgreSQL], or PostgreSQL-compatible database to operate.

The lowest supported version is 11. You are encouraged to use the [newest available version].

Depending on available resources, individual preferences, or organizational policies,
you will have to choose between a [managed](#managed-solutions), or [self-hosted](#self-hosting) solution.

## Managed Solutions

The official PostgreSQL website hosts a [list of well-known commercial hosting providers].

Popular choices include:

* [Amazon RDS for PostgreSQL](https://aws.amazon.com/rds/postgresql/)
* [Aiven for PostgreSQL](https://aiven.io/postgresql)
* [Azure Database for PostgreSQL](https://azure.microsoft.com/en-us/products/postgresql/)
* [Google Cloud SQL for PostgreSQL](https://cloud.google.com/sql/docs/postgres/)

We are not actively testing against cloud offerings. But as a rule of thumb, solutions offering "vanilla" PostgreSQL, 
or extensions of it (for example [Neon] or [Timescale]), will most definitely work with Dependency-Track.

The same is not necessarily true for platforms based on heavily modified PostgreSQL, or even entire re-implementations
such as [CockroachDB] or [YugabyteDB]. Such solutions make certain trade-offs to achieve higher levels of scalability,
which might impact functionality that Dependency-Track relies on. If you'd like to see support for those, please [let us know]!

## Self-Hosting

### Bare Metal / Docker

For Docker deployments, use the official [`postgres`](https://hub.docker.com/_/postgres) image.

!!! warning
    Do **not** use the `latest` tag! You may end up doing a major version upgrade without knowing it,
    ultimately breaking your database! Pin the tag to at least the major version (e.g. `16`), or better
    yet the minor version (e.g. `16.2`). Refer to [Upgrades](#upgrades) to upgrade instructions.

For bare metal deloyments, it's usually best to install PostgreSQL from your distribution's package repository.
See for example:

* [PostgreSQL instructions for Debian](https://wiki.debian.org/PostgreSql)
* [Install and configure PostgreSQL on Ubuntu](https://ubuntu.com/server/docs/databases-postgresql)
* [Using PostgreSQL with Red Hat Enterprise Linux](https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/9/html/configuring_and_using_database_servers/using-postgresql_configuring-and-using-database-servers)

To get the most out of your Dependency-Track installation, we recommend to run PostgreSQL on a separate machine
than the application containers. You want PostgreSQL to be able to leverage the entire machine's resources,
without being impacted by other applications.

For smaller and non-critical deployments, it is totally fine to run everything on a single machine.

#### Configuration

You should be aware that the default PostgreSQL configuration is *extremely* conservative.
It is intended to make PostgreSQL usable on minimal hardware, which is great for testing,
but can seriously cripple performance in production environments.
Not adjusting it to your specific setup will most certainly leave performance on the table.

If you're lucky enough to have access to professional database administrators, ask them for help.
They will know your organisation's best practices and can guide you in adjusting it for Dependency-Track.

If you're not as lucky, we can wholeheartedly recommend [PGTune]. Given a bit of basic info about your system,
it will provide a sensible baseline configuration. For the *DB Type* option, select `Online transaction processing system`.

![Example output of PGTune](../images/operations_database_pgtune.png)

The `postgresql.conf` is usually located at `/var/lib/postgresql/data/postgresql.conf`.
Most of these settings require a restart of the application.

In a Docker Compose setup, you can alternatively apply the desired configuration via command line flags.
For example:

```yaml
services:
  postgres:
    image: postgres:16
    command: >-
      postgres
        -c 'shared_buffers=2GB'
        -c 'effective_cache_size=6GB'
```

!!! note
    Got more tips to configure or tune PostgreSQL, that may be helpful to others?
    We'd love to include it in the docs, please do raise a PR!

#### Upgrades

Follow the [official upgrading guide]. Be sure to select the version of the documentation that corresponds to the
PostgreSQL version you are running.

!!! warning
    Pay attention to the fact that **major version upgrades usually require a backup-and-restore cycle**, due to potentially
    breaking changes in the underlying data storage format. Minor version upgrades are usually safe to perform in a
    rolling manor.

### Kubernetes

We generally advise **against** running PostgreSQL on Kubernetes, unless you *really* know what you're doing.
Wielding heavy machinery such as [Postgres Operator] is not something you should do lightheartedly.

If you know what you're doing, you definitely don't need advice from us. Smooth sailing! ⚓️

## Schema Migrations

Schema migrations are performed automatically by the API server upon startup. It leverages [Liquibase] for doing so.
There is usually no manual action required when upgrading from an older Dependency-Track version, unless explicitly
stated otherwise in the release notes.

This behavior can be turned off by setting the `RUN_MIGRATIONS` environment variable of the API server container to `false`.

It is possible to use different credentials for migrations than for the application itself.
This can be achieved with the following environment variables:

* `DATABASE_MIGRATION_URL`
* `DATABASE_MIGRATION_USERNAME`
* `DATABASE_MIGRATION_PASSWORD`

The above with default to the main database credentials if not provided explicitly.

[CockroachDB]: https://www.cockroachlabs.com/
[Liquibase]: https://www.liquibase.com/
[Neon]: https://neon.tech/
[PGTune]: https://pgtune.leopard.in.ua/
[PostgreSQL]: https://www.postgresql.org/
[Postgres Operator]: https://github.com/zalando/postgres-operator
[Timescale]: https://www.timescale.com/
[YugabyteDB]: https://www.yugabyte.com/
[let us know]: https://github.com/DependencyTrack/hyades/issues/new?assignees=&labels=enhancement&projects=&template=enhancement-request.yml
[list of well-known commercial hosting providers]: https://www.postgresql.org/support/professional_hosting/
[newest available version]: https://www.postgresql.org/support/versioning/
[official upgrading guide]: https://www.postgresql.org/docs/current/upgrading.html