# quarkus-config-dependencytrack

`quarkus-config-dependencytrack` is a [Quarkus extension] that bridges Quarkus' [configuration framework]
with Dependency-Track's existing configuration storage, namely the `CONFIGPROPERTY` database table.

### Config Source

The `database` config source is enabled per default. It can be disabled by setting the following property:

```yml
quarkus.config.source.dtrack.database.enabled=false
```

> [!NOTE]
> The config source requires a [JDBC datasource].

When enabled, this config source takes precedence over all [default sources].
If a property is defined in the database, it cannot be overwritten via environment variables or system properties.

Properties sourced from the `CONFIGPROPERTY` table **must** be prefixed with `dtrack`.

For example, to access the following property from `CONFIGPROPERTY`:

| `GROUPNAME` | `PROPERTYNAME` | `PROPERTYVALUE` |
|:------------|:---------------|:----------------|
| `internal`  | `cluster.id`   | `foo-bar-baz`   |

You'd do this in Quarkus:

```java
@ConfigProperty(name = "dtrack.internal.cluster.id")
String clusterId;
```

> [!NOTE]
> Injecting a property via `@ConfigProperty` will *not* reflect changes made to the
> property at runtime (i.e. users updating configs via UI). If properties are expected
> to change at runtime, inject a `jakarta.inject.Provider` instead:
> 
> ```java
> @ConfigProperty(name = "dtrack.integrations.jira.username")
> Provider<Optional<String>> jiraUsername;
> ```
> 
> Calling `jiraUsername.get()` will now always reach to the underlying config source.

Per default, properties are cached for **5min** to reduce excessive database queries.
Caching can be controlled via the following properties:

```yml
# Disable caching (defaults to true)
quarkus.config.source.dtrack.database.cache.enabled=false

# Change caching duration to 10min (defaults to 5min)
quarkus.config.source.dtrack.database.cache.expire-after-write=PT10M
```

[JDBC datasource]: https://quarkus.io/guides/datasource#configure-a-jdbc-datasource
[Quarkus extension]: https://quarkus.io/guides/writing-extensions
[configuration framework]: https://quarkus.io/guides/config-reference
[default sources]: https://quarkus.io/guides/config-reference#configuration-sources
