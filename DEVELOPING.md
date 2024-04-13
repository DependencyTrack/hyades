# Hacking on OWASP Dependency-Track

Want to hack on Hyades, the upcoming Dependency-Track v5? Awesome, here's what you need to know to get started!

> Please be sure to read [`CONTRIBUTING.md`](./CONTRIBUTING.md) and
> [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) as well.

> [!WARNING]
> This document is still incomplete. We're working on it!

## Repositories

* [DependencyTrack/hyades](https://github.com/DependencyTrack/hyades) - 
* [DependencyTrack/hyades-apiserver](https://github.com/DependencyTrack/hyades) - 
* [DependencyTrack/hyades-frontend](https://github.com/DependencyTrack/frontend) - The frontend, a single page application (SPA), based on JavaScript and [Vue](https://vuejs.org/).

> [!NOTE]
> The `hyades` and `hyades-apiserver` repositories are split for historical reasons.
> We are planning to merge them (https://github.com/DependencyTrack/hyades/issues/932), 
> which should result in less overhead and more opportunities to share code between them.

## Prerequisites

There are a few things you'll need on your journey:

* JDK<sup>1</sup> 21+ ([Temurin](https://adoptium.net/temurin/releases) distribution recommended)
* [Maven]<sup>1</sup> 3.9+ (comes bundled with IntelliJ and Eclipse)
* NodeJS<sup>2</sup> 20+
* A Java and JavaScript capable IDE of your preference (we recommend IntelliJ<sup>3</sup>, but any other IDE is fine as well)
* [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/)
* [Docker Compose](https://docs.docker.com/compose/) or [Podman Compose](https://github.com/containers/podman-compose)

> [!NOTE]
> *<sup>1</sup> We recommend [sdkman](https://sdkman.io/) to install Java and Maven. When working in a corporate environment,
> you should obviously prefer the packages provided by your organization.*
> 
> *<sup>2</sup> If you need to juggle multiple NodeJS versions on your system, consider using
> [nvm](https://github.com/nvm-sh/nvm) to make this more bearable.*
> 
> *<sup>3</sup> We provide common [run configurations](https://www.jetbrains.com/help/idea/run-debug-configuration.html) for IntelliJ
> in the `.idea/runConfigurations` directories of each repository for convenience. 
> IntelliJ will automatically pick those up when you open this repository.*

## Core Technologies

Knowing about the core technologies used by the API server may help you with understanding its codebase.

### Infrastructure

| Technology                                | Purpose               |
|:------------------------------------------|:----------------------|
| [PostgreSQL](https://www.postgresql.org/) | Database              |
| [Apache Kafka](https://kafka.apache.org/) | Messaging / Streaming |

### API Server

| Technology                                                                                  | Purpose                         |
|:--------------------------------------------------------------------------------------------|:--------------------------------|
| [JAX-RS](https://projects.eclipse.org/projects/ee4j.rest)                                   | REST API specification          |
| [Jersey](https://eclipse-ee4j.github.io/jersey/)                                            | JAX-RS implementation           |
| [Java Data Objects (JDO)](https://db.apache.org/jdo/)                                       | Persistence specification       |
| [DataNucleus](https://www.datanucleus.org/products/accessplatform/jdo/getting_started.html) | JDO implementation              |
| [JDBI](https://jdbi.org/#_introduction_to_jdbi_3)                                           | Lightweight database operations |
| [Liquibase]                                                                                 | Database migrations             |
| [Confluent Parallel Consumer](https://github.com/confluentinc/parallel-consumer)            | Kafka message processing        |
| [Jetty](https://www.eclipse.org/jetty/)                                                     | Servlet Container               |
| [Alpine](https://github.com/stevespringett/Alpine)                                          | Framework / Scaffolding         |
| [Maven]                                                                                     | Package manager / Build tool    |
| [Java]                                                                                      | Programming language            |

### Hyades Services

| Technology                     | Purpose                      |
|:-------------------------------|:-----------------------------|
| [Quarkus](https://quarkus.io/) | Framework / Scaffolding      |
| [Maven]                        | Package manager / Build tool |
| [Java]                         | Programming language         |

### Frontend

| Technology                         | Purpose                      |
|:-----------------------------------|:-----------------------------|
| [webpack](https://webpack.js.org/) | Asset bundler                |
| [Vue.js](https://vuejs.org/)       | Framework                    |
| NPM                                | Package manager / Build tool |
| JavaScript                         | Programming language         |

## Building

### API server

To build a JAR:

```shell
mvn clean package -Penhance -Pembedded-jetty -DskipTests -Dlogback.configuration.file=src/main/docker/logback.xml
```

### Hyades Services

To build JARs for all services:

```shell
mvn clean package -DskipTests
```

To build JARs *and container images* for all services:

```shell
mvn clean package -DskipTests -Dquarkus.container-image.build=true -Dquarkus.container-image.additional-tags=local
```

To build native executables for all services (requires GraalVM to be installed):

```shell
mvn clean package -DskipTests -Dnative
```

### Frontend

TBD

## Database Migrations

In contrast to Dependency-Track v4 and earlier, Dependency-Track v5 manages database migrations with [Liquibase].
The database schema is still *owned* by the API server though. It will execute migrations upon startup,
unless explicitly disabled via [`RUN_MIGRATIONS`](https://dependencytrack.github.io/hyades/latest/reference/configuration/api-server/#runmigrations).

[Liquibase] operates with the concept of [changelogs](https://docs.liquibase.com/concepts/changelogs/home.html).
For the sake of better visibility, Dependency-Track uses separate changelogs for each release version.
Individual changelogs are referenced by [`changelog-main.xml`](https://github.com/DependencyTrack/hyades-apiserver/blob/main/src/main/resources/migration/changelog-main.xml).

Stored procedures and custom SQL functions are treated differently: They will be (re-) created whenever their
content changes. Their sources are located in the [`procedures`](https://github.com/DependencyTrack/hyades-apiserver/tree/main/src/main/resources/migration/procedures) directory.

### Adding Migrations

When adding a new [changeset](https://docs.liquibase.com/concepts/changelogs/changeset.html):

* The changeset ID **must** follow the `v<MAJOR>.<MINOR>.<PATCH>-<NUM>` format, where:
  * `MAJOR`, `MINOR`, and `PATCH` follow the respective release version
  * `NUM` is an incrementing number, starting at `1` for the first changeset of the release
* The author **must** correspond to your GitHub username
* Prefer [built-in change types](https://docs.liquibase.com/change-types/home.html)
  * Use the [`sql` change type](https://docs.liquibase.com/change-types/sql.html) if no fitting built-in exists
  * Use a [custom change](https://docs.liquibase.com/change-types/custom-change.html) *in edge cases*, when additional computation is required
* When using custom changes:
  * Put the change class in the [`org.dependencytrack.persistence.migration.change`](https://github.com/DependencyTrack/hyades-apiserver/tree/main/src/main/java/org/dependencytrack/persistence/migration/change) package
  * Avoid accessing other code from the code base, changes **must not** depend on domain logic

### Making Schema Changes Available to Hyades Services

Because the schema is owned by the API server, and the API server is also responsible for executing migrations,
other services that access the database must replicate the current schema, in order to run tests against it.

Currently, this is achieved by:

1. Having [Liquibase] generate the schema SQL based on the changelog
2. Adding the `schema.sql` file as resource to the [`commons-persistence`](https://github.com/DependencyTrack/hyades/blob/c1463a80c8ad45e0ef401292cf29dbd37a7de308/commons-persistence/src/main/resources/schema.sql) module
3. Having all services that require database access depend on `commons-persistence`
4. Configuring [Quarkus Dev Services](https://quarkus.io/guides/databases-dev-services) to initialize new database containers with `schema.sql`
    * Using the `quarkus.datasource.devservices.init-script-path` property

The schema can be generated using the [`dbschema-generate.sh`](https://github.com/DependencyTrack/hyades-apiserver/blob/794e9eaa2991f961223653b293c46ce64bc7e0ce/dev/scripts/dbschema-generate.sh)
script in the `hyades-apiserver` repository:

```shell
./dev/scripts/dbschema-generate.sh
```

> [!NOTE]
> Because Liquibase requires a live database to run against, the script will launch a temporary PostgreSQL container.

## Documentation

User-facing documentation is implemented with [MkDocs] and [Material for MkDocs].
The sources are located in [`docs`](docs). Changes to the documentation are automatically
deployed to GitHub pages, using the [`deploy-docs.yml`](.github/workflows/deploy-docs.yml) GitHub Actions workflow.
Once deployed, the documentation is available at <https://dependencytrack.github.io/hyades/latest>.

For local building and rendering of the docs, use the [`docs-dev.sh`](scripts/docs-dev.sh) script:

```shell
./scripts/docs-dev.sh
```

It will launch a development server that listens on <http://localhost:8000> and reloads whenever changes
are made to the documentation sources. The script requires the `docker` command to be available.

### Configuration Documentation

To make it easier for users to discover available configuration options (i.e. environment variables),
we generate human-readable documentation for it, and include it in our [docs](docs/reference/configuration).

Configuration documentation is generated from `application.properties` files.
We use the [`GenerateConfigDocs`](https://github.com/DependencyTrack/jbang-catalog/blob/main/GenerateConfigDocs.java) [JBang] script for this:

```
Usage: GenerateConfigDocs [--include-hidden] [-o=OUTPUT_PATH] -t=TEMPLATE_FILE
                          PROPERTIES_FILE
      PROPERTIES_FILE        The properties file to generate documentation for
      --include-hidden       Include hidden properties in the output
  -o, --output=OUTPUT_PATH   Path to write the output to, will write to STDOUT
                               if not provided
  -t, --template=TEMPLATE_FILE
                             The Pebble template file to use for generation
```

> [!NOTE]
> Usually you do not need to run the script yourself. We have a GitHub Actions workflow
> ([`update-config-docs.yml`](.github/workflows/update-config-docs.yml)) that does that
> automatically whenever a modification to `application.properties` files is pushed to
> the `main` branch. It also works across repositories, i.e. it will be triggered for
> changes in the `hyades-apiserver` repository as well.

To generate documentation for the API server, you would run:

```shell
jbang gen-config-docs@DependencyTrack \
    -t ./scripts/config-docs.md.peb \
    -o ./docs/reference/configuration/api-server.md \
    ../hyades-apiserver/src/main/resources/application.properties
```

The script leverages comments on property definitions to gather metadata. Other than a property's description,
the following *annotations* are supported to provide further information:

| Annotation  | Description                                                                                                                       |
|:------------|:----------------------------------------------------------------------------------------------------------------------------------|
| `@category` | Allows for categorization / grouping of related properties                                                                        |
| `@default`  | To be used for cases where the default value is implicit, for example when it is inherited from the framework or other properties |
| `@example`  | To give an idea of what a valid value may look like, when it's not possible to provide a sensible default value                   |
| `@hidden`   | Marks a property as to-be-excluded from the generated docs                                                                        |
| `@required` | Marks a property as required                                                                                                      |
| `@type`     | Defines the type of the property                                                                                                  |

For example, a properly annotated property might look like this:

```ini
# Defines the path to the secret key to be used for data encryption and decryption.
# The key will be generated upon first startup if it does not exist.
#
# @category: General
# @default:  ${alpine.data.directory}/keys/secret.key
# @type:     string
alpine.secret.key.path=
```

The script is able to index properties that are commented out as well, for example:

```ini
# Foo bar baz.
#
# @category: General
# @example:  Some example value
# @type:     string
# foo.bar.baz=
```

This can be useful when it's not possible to provide a sensible default, and providing the property without
a value would break something. Generally though, you should always prefer setting a sensible default.

Output is generated based on a customizable [Pebble] template (currently [`config-docs.md.peb`](scripts/config-docs.md.peb)).

[Java]: https://dev.java/
[JBang]: https://www.jbang.dev/
[Liquibase]: https://www.liquibase.com/
[Maven]: https://maven.apache.org/
[MkDocs]: https://www.mkdocs.org/
[Material for MkDocs]: https://squidfunk.github.io/mkdocs-material/
[Pebble]: https://pebbletemplates.io/