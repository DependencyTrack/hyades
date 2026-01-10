Dependency-Track's configuration system is based on [MicroProfile Config],
enabling it to support multiple [sources](#sources).

!!! tip
    A comprehensive list of supported config properties can be found in
    the [configuration reference](../../reference/configuration/api-server.md).

## Sources

Config properties are loaded, *in order*, from the following sources:

1. JVM system properties
2. Environment variables
3. `${cwd}/.env` file
4. `${cwd}/config/application.properties` file
5. `application.properties` embedded in the application

!!! tip
    `${cwd}` refers to the **c**urrent **w**orking **d**irectory.
    When running an official container image, it is `/opt/owasp/dependency-track`.

Once a value is found, later sources will not be checked. For example, when Dependency-Track
attempts to look up the config property `foo.bar`, the environment variable `FOO_BAR=123` is
ignored if the JVM was launched with `-Dfoo.bar=321`.

## Expressions

Configuration values may use expressions, indicated by `${...}`, to reference each other:

```ini linenums="1"
dt.datasource.foo.url=jdbc:postgresql://localhost:5432/dtrack
dt.datasource.bar.url=${dt.datasource.foo.url}
```

This is useful to avoid redundant definition of identical values.

## Environment Variable Mapping

The canonical representation of properties uses alphanumeric characters,
separated by hyphens (`-`) and periods (`.`). For example:

```ini linenums="1"
foo.BAR-baz=123
```

Environment variables commonly only support alphanumeric characters and underscores (`_`).
To bridge this gap, Dependency-Track will use the following matching strategies,
as [defined](https://download.eclipse.org/microprofile/microprofile-config-3.1/microprofile-config-spec-3.1.html#default_configsources.env.mapping)
by [MicroProfile Config]:

> 1. Exact match (i.e. `foo.BAR-baz`)
> 2. Replace each character that is neither alphanumeric nor `_` with `_` (i.e. `foo_BAR_baz`)
> 3. Replace each character that is neither alphanumeric nor `_` with `_`; then convert the name to upper case (i.e. `FOO_BAR_BAZ`)

!!! tip
    The [configuration reference](../../reference/configuration/api-server.md) includes the correct
    environment variable names for each listed config property.

[MicroProfile Config]: https://microprofile.io/specifications/config/