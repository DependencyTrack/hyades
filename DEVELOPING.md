# Hacking on OWASP Dependency-Track

Want to hack on Hyades, the upcoming Dependency-Track v5? Awesome, here's what you need to know to get started!

> Please be sure to read [`CONTRIBUTING.md`](./CONTRIBUTING.md) and
> [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) as well.

> [!WARNING]
> This document is still incomplete. We're working on it!

## Configuration Documentation

To make it easier for users to discover available configuration options,
we generate human-readable documentation for it, and include it in our [docs](docs/reference/configuration).

Configuration documentation is generated from `application.properties` files.
We use the [`GenerateConfigDocs`](scripts/GenerateConfigDocs.java) [JBang] script for this:

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

To generate documentation for the API server, you would run:

```shell
jbang scripts/GenerateConfigDocs.java \
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

Output is generated based on a customizable [Pebble] template (currently [`config-docs.md.peb`](scripts/config-docs.md.peb)).

[JBang]: https://www.jbang.dev/
[Pebble]: https://pebbletemplates.io/