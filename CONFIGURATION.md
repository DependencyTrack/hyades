# Configuration

All available configuration options used by all applications are listed in their respective `application.properties`.  
Options can be provided via environment variables as well, refer to the [Quarkus docs] for details.

Not all options are supposed to be tweaked by users though. This document contains an overview of all
options that are expected to be changed by users.

### Notification Publisher

| Environment Variable                      | Description                                        | Default          |               Required               |
|:------------------------------------------|:---------------------------------------------------|:-----------------|:------------------------------------:|
| `QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS` | Comma-separated list of Kafka servers              | `localhost:9092` |                  ✅                   |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`        | Number of Kafka Streams threads                    | `3`              |                  ❌                   |
| `QUARKUS_DATASOURCE_DB_KIND`              | The database type                                  | `postgresql`     |                  ✅                   |
| `QUARKUS_DATASOURCE_JDBC_URL`             | The database JDBC URL                              | -                |                  ✅                   |
| `QUARKUS_DATASOURCE_USERNAME`             | The database username                              | -                |                  ✅                   |
| `QUARKUS_DATASOURCE_PASSWORD`             | The database password                              | -                |                  ✅                   |
| `QUARKUS_MAILER_FROM`                     | The sender name for email notifications            | -                | When email notifications are enabled |
| `QUARKUS_MAILER_HOST`                     | Address of the mail server for email notifications | -                | When email notifications are enabled |
| `QUARKUS_MAILER_PORT`                     | Port of the mail server for email notifications    | -                | When email notifications are enabled |
| `QUARKUS_MAILER_SSL`                      | Use SSL / TLS to communicate with the email server | `false`          |                  -                   |
| `QUARKUS_MAILER_START_TLS`                | Use StartTLS to communicate with the email server  | `DISABLED`       | When email notifications are enabled |
| `QUARKUS_MAILER_USERNAME`                 | Username to authenticate with the email server     | -                | When email notifications are enabled |
| `QUARKUS_MAILER_PASSWORD`                 | Password to authenticate with the email server     | -                | When email notifications are enabled |

> **Note**
> Refer
> to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/notification-publisher/src/main/resources/application.properties)
> for a complete overview of available config options.

### Repository Meta Analyzer

| Environment Variable                      | Description                           | Default          | Required |
|:------------------------------------------|:--------------------------------------|:-----------------|:--------:|
| `QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS` | Comma-separated list of Kafka servers | `localhost:9092` |    ✅     |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`        | Number of Kafka Streams threads       | `3`              |    ❌     |
| `QUARKUS_DATASOURCE_DB_KIND`              | The database type                     | `postgresql`     |    ✅     |
| `QUARKUS_DATASOURCE_JDBC_URL`             | The database JDBC URL                 | -                |    ✅     |
| `QUARKUS_DATASOURCE_USERNAME`             | The database username                 | -                |    ✅     |
| `QUARKUS_DATASOURCE_PASSWORD`             | The database password                 | -                |    ✅     |

> **Note**
> Refer
> to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/repository-meta-analyzer/src/main/resources/application.properties)
> for a complete overview of available config options.

### Vulnerability Analyzer

| Environment Variable                      | Description                                           | Default          |       Required       |
|:------------------------------------------|:------------------------------------------------------|:-----------------|:--------------------:|
| `QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS` | Comma-separated list of Kafka servers                 | `localhost:9092` |          ✅           |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`        | Number of Kafka Streams threads                       | `3`              |          ❌           |
| `QUARKUS_DATASOURCE_DB_KIND`              | The database type                                     | `postgresql`     |          ✅           |
| `QUARKUS_DATASOURCE_JDBC_URL`             | The database JDBC URL                                 | -                |          ✅           |
| `QUARKUS_DATASOURCE_USERNAME`             | The database username                                 | -                |          ✅           |
| `QUARKUS_DATASOURCE_PASSWORD`             | The database password                                 | -                |          ✅           |
| `SCANNER_INTERNAL_ENABLED`                | Enable the internal vulnerability scanner             | `true`           |          ❌           |
| `SCANNER_OSSINDEX_ENABLED`                | Enable the OSS Index vulnerability scanner            | `true`           |          ❌           |
| `SCANNER_OSSINDEX_API_USERNAME`           | OSS Index API username                                | -                |          ❌           |
| `SCANNER_OSSINDEX_API_TOKEN`              | OSS Index API token                                   | -                |          ❌           |
| `SCANNER_OSSINDEX_BATCH_INTERVAL`         | Max time to wait before submitting incomplete batches | `5S`             |          ❌           |
| `SCANNER_SNYK_ENABLED`                    | Enable the Snyk vulnerability scanner                 | `false`          |          ❌           |
| `SCANNER_SNYK_API_ORG_ID`                 | Snyk organization ID                                  | -                | When Snyk is enabled |
| `SCANNER_SNYK_API_TOKENS`                 | Comma-separated list of Snyk API tokens               | -                | When Snyk is enabled |
| `SCANNER_SNYK_API_VERSION`                | Version of the Snyk API to use                        | `2022-12-15`     | When Snyk is enabled |
| `SCANNER_SNYK_SEVERITY_SOURCE`            | Preferred source of vulnerability severities          | `NVD`            | When Snyk is enabled |

> **Note**
> Refer
> to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/vulnerability-analyzer/src/main/resources/application.properties)
> for a complete overview of available config options.

[Quarkus docs]: https://quarkus.io/guides/config-reference#configuration-sources
