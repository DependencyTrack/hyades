# Data Sources

!!! warning
    This document is only applicable to the API server component.

Data sources are logical objects through which database connections can be acquired.

## How It Works

Data sources are configured using properties in the following format:

```ini
dt.datasource.<name>.<property>=<value>
```

For available properties, and their environment variable equivalents, refer to the [configuration reference].

!!! tip
    When `<name>` is omitted, `default` is assumed.
    This means that `dt.datasource.url` and `dt.datasource.default.url` are treated equally.

The `default` data source is *required*. It's used to serve the REST API,
as well as the vast majority of background processing.

Certain features of Dependency-Track can be configured to use
a different data source than `default`. For example, the 
[*database* secret management provider](../../usage/secret-management/providers.md#database).
Instead of database connection details, they accept a *data source name*, e.g. via
[`dt.secret-management.database.datasource.name`](../../reference/configuration/api-server.md#dtsecret-managementdatabasedatasourcename).

The main use case for this capability is to prevent features from
saturating the `default` connection pool and vice versa. In the future,
this mechanism may also be used to allow configuration of read-replicas
to serve certain data faster without impacting the primary database.

Here's how configuring multiple data sources would look in practice:

```ini linenums="1"
# Configure the default data source
dt.datasource.url=jdbc:postgresql://localhost:5432/dtrack
dt.datasource.username=dtrack
dt.datasource.password=dtrack
dt.datasource.pool.enabled=true
dt.datasource.pool.max-size=20

# Configure the secret management data source
dt.datasource.secretmgt.url=jdbc:postgresql://localhost:5432/dtrack
dt.datasource.secretmgt.username=secrets
dt.datasource.secretmgt.password=secrets
dt.datasource.secretmgt.pool.enabled=true
dt.datasource.secretmgt.pool.max-size=5

# Configure secret management to use separate data source
dt.secret-management.provider=database
dt.secret-management.database.datasource.name=secretmgt
```

!!! note
    Data sources are instantiated on first use. You can configure as many
    data sources as you like, but unless they're being used by a feature,
    they will not be created.

## Connection Pooling

### Local

Local connection pooling involves each Dependency-Track instance maintaining
its own pool of database connections. This works well for small to medium deployments
(i.e., 1-5 instances).

Connection pooling can be configured using the following properties:

* [`dt.datasource.<name>.pool.enabled`](../../reference/configuration/api-server.md#dtdatasourcepoolenabled)
* [`dt.datasource.<name>.pool.url`](../../reference/configuration/api-server.md#dtdatasourceurl)
* [`dt.datasource.<name>.pool.username`](../../reference/configuration/api-server.md#dtdatasourceusername)
* [`dt.datasource.<name>.pool.password`](../../reference/configuration/api-server.md#dtdatasourcepassword)
* [`dt.datasource.<name>.pool.password-file`](../../reference/configuration/api-server.md#dtdatasourcepassword-file)
* [`dt.datasource.<name>.pool.max-size`](../../reference/configuration/api-server.md#dtdatasourcepoolmax-size)
* [`dt.datasource.<name>.pool.min-idle`](../../reference/configuration/api-server.md#dtdatasourcepoolmin-idle)
* [`dt.datasource.<name>.pool.max-lifetime-ms`](../../reference/configuration/api-server.md#dtdatasourcepoolmax-lifetime-ms)
* [`dt.datasource.<name>.pool.idle-timeout-ms`](../../reference/configuration/api-server.md#dtdatasourcepoolidle-timeout-ms)

If you plan on deploying multiple instances, make sure your PostgreSQL database
can handle the sum of connections from all connection pools.

Say you configure `dt.datasource.pool.max-size=30`, and you deploy 5 instances,
your database should be configured to handle up to `30 * 5 = 150` connections.
By default, PostgreSQL limits connections to 100. You can tweak this using
the [`max_connections`](https://postgresqlco.nf/doc/en/param/max_connections/) setting.

### Centralised

For large deployments (i.e., upwards of 5 instances), it can become undesirable for
each instance to maintain its own connection pool. In this case, you can leverage
centralised connection pools such as [PgBouncer].

!!! warning
    When using a central connection pooler, you **must** disable local connection pooling
    for all data sources, using [`dt.datasource.<name>.pool.enabled=false`](../../reference/configuration/api-server.md#dtdatasourcepoolenabled).

#### Constraints

Before proceeding, take note of the following constraints:

* Only `session` and `transaction` pooling modes are supported. `transaction` is recommended.
* Initialisation tasks, which include database migrations, **must** connect to the
  database directly, bypassing the connection pooler, when using pooling mode `transaction`.
    * To prevent concurrent initialisation, session-level PostgreSQL advisory locks are used,
      which are not supported with the `transaction` pooling mode.
    * To facilitate this, initialisation tasks can be executed in dedicated containers,
      and / or using separate data sources.

#### Example

```yaml linenums="1"
services:
  postgres:
    image: postgres
    environment:
      POSTGRES_DB: "dtrack"
      POSTGRES_USER: "dtrack"
      POSTGRES_PASSWORD: "dtrack"

  pgbouncer:
    image: bitnami/pgbouncer
    environment:
      POSTGRESQL_HOST: "postgres"
      POSTGRESQL_PORT: "5432"
      POSTGRESQL_USERNAME: "dtrack"
      POSTGRESQL_PASSWORD: "dtrack"
      POSTGRESQL_DATABASE: "dtrack"
      PGBOUNCER_DATABASE: "dtrack"
      PGBOUNCER_PORT: "6432"
      PGBOUNCER_POOL_MODE: "transaction"
      PGBOUNCER_DEFAULT_POOL_SIZE: "30"

  apiserver:
    image: ghcr.io/dependencytrack/hyades-apiserver
    environment:
      # Configure the default data source:
      # - Points to PgBouncer, NOT Postgres directly.
      # - Pooling is DISABLED.
      DT_DATASOURCE_URL: "jdbc:postgresql://pgbouncer:6432/dtrack"
      DT_DATASOURCE_USERNAME: "dtrack"
      DT_DATASOURCE_PASSWORD: "dtrack"
      DT_DATASOURCE_POOL_ENABLED: "false"
      # Configure the data source for initialisation tasks:
      # - Points to Postgres directly, NOT PgBouncer.
      # - Pooling is DISABLED.
      DT_DATASOURCE_INIT_URL: "jdbc:postgresql://postgres:5432/dtrack"
      DT_DATASOURCE_INIT_USERNAME: "dtrack"
      DT_DATASOURCE_INIT_PASSWORD: "dtrack"
      DT_DATASOURCE_INIT_POOL_ENABLED: "false"
      # Configure initialisation tasks to use the above data source.
      INIT_TASKS_DATASOURCE_NAME: "init"
```

[PgBouncer]: https://www.pgbouncer.org/
[configuration reference]: ../../reference/configuration/api-server.md#database