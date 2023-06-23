# Configuration

All available configuration options used by all applications are listed in their respective `application.properties`.  
Options can be provided via environment variables as well, refer to the [Quarkus docs] for details.

Not all options are supposed to be tweaked by users though. This document contains an overview of all
options that are expected to be changed by users.

### Kafka Topic Configuration

Kafka topics (including internal topics) can be configured with custom prefix. In order to provide custom prefix, below
environment variable can be used.

| Environment Variable | Description            | Default | Required |
|:---------------------|:-----------------------|:--------|:--------:|
| `API_TOPIC_PREFIX`   | Prefix for topic names | -       |    ❌     |

### Notification Publisher

| Environment Variable                           | Description                                         | Default          |               Required               |
|:-----------------------------------------------|:----------------------------------------------------|:-----------------|:------------------------------------:|
| `KAFKA_BOOTSTRAP_SERVERS`                      | Comma-separated list of Kafka servers               | `localhost:9092` |                  ✅                   |
| `PARALLEL_CONSUMER_MAX_CONCURRENCY`            | Number of threads to process notifications with     | `6`              |                  ✅                   |
| `PARALLEL_CONSUMER_RETRY_INITIAL_DELAY`        | Initial delay before retrying notification delivery | `3S`             |                  ✅                   |
| `PARALLEL_CONSUMER_RETRY_MULTIPLIER`           | Multiplier for retry delays                         | `0.3`            |                  ✅                   |
| `PARALLEL_CONSUMER_RETRY_RANDOMIZATION_FACTOR` | Randomization factory for jitter in retry delays    | `0.3`            |                  ❌                   |
| `PARALLEL_CONSUMER_RETRY_MAX_DURATION`         | Maximum duration of delays between retry attempts   | `2M`             |                  ✅                   |
| `QUARKUS_DATASOURCE_DB_KIND`                   | The database type                                   | `postgresql`     |                  ✅                   |
| `QUARKUS_DATASOURCE_JDBC_URL`                  | The database JDBC URL                               | -                |                  ✅                   |
| `QUARKUS_DATASOURCE_USERNAME`                  | The database username                               | -                |                  ✅                   |
| `QUARKUS_DATASOURCE_PASSWORD`                  | The database password                               | -                |                  ✅                   |
| `QUARKUS_LOG_CONSOLE_JSON`                     | Enable logging in JSON format                       | `false`          |                  ❌                   |
| `QUARKUS_MAILER_FROM`                          | The sender name for email notifications             | -                | When email notifications are enabled |
| `QUARKUS_MAILER_HOST`                          | Address of the mail server for email notifications  | -                | When email notifications are enabled |
| `QUARKUS_MAILER_PORT`                          | Port of the mail server for email notifications     | -                | When email notifications are enabled |
| `QUARKUS_MAILER_SSL`                           | Use SSL / TLS to communicate with the email server  | `false`          |                  -                   |
| `QUARKUS_MAILER_START_TLS`                     | Use StartTLS to communicate with the email server   | `DISABLED`       | When email notifications are enabled |
| `QUARKUS_MAILER_USERNAME`                      | Username to authenticate with the email server      | -                | When email notifications are enabled |
| `QUARKUS_MAILER_PASSWORD`                      | Password to authenticate with the email server      | -                | When email notifications are enabled |

> **Note**  
> Refer to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/notification-publisher/src/main/resources/application.properties)
> for a complete overview of available config options.

### Repository Meta Analyzer

| Environment Variable               | Description                           | Default          | Required |
|:-----------------------------------|:--------------------------------------|:-----------------|:--------:|
| `KAFKA_BOOTSTRAP_SERVERS`          | Comma-separated list of Kafka servers | `localhost:9092` |    ✅     |
| `KAFKA_STREAMS_NUM_STREAM_THREADS` | Number of Kafka Streams threads       | `3`              |    ❌     |
| `QUARKUS_DATASOURCE_DB_KIND`       | The database type                     | `postgresql`     |    ✅     |
| `QUARKUS_DATASOURCE_JDBC_URL`      | The database JDBC URL                 | -                |    ✅     |
| `QUARKUS_DATASOURCE_USERNAME`      | The database username                 | -                |    ✅     |
| `QUARKUS_DATASOURCE_PASSWORD`      | The database password                 | -                |    ✅     |
| `QUARKUS_LOG_CONSOLE_JSON`         | Enable logging in JSON format         | `false`          |    ❌     |

> **Note**  
> Refer to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/repository-meta-analyzer/src/main/resources/application.properties)
> for a complete overview of available config options.

### Vulnerability Analyzer

| Environment Variable                    | Description                                                                 | Default                |       Required       |
|:----------------------------------------|:----------------------------------------------------------------------------|:-----------------------|:--------------------:|
| `KAFKA_BOOTSTRAP_SERVERS`               | Comma-separated list of Kafka servers                                       | `localhost:9092`       |          ✅           |
| `KAFKA_SSL_ENABLED`                     | SSL enabled for using kafka broker                                          | `false`                |          ❌           |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`      | Number of Kafka Streams threads                                             | `3`                    |          ❌           |
| `STATE_STORE_TYPE`                      | Whether to use in-memory or persistent (RocksDB) Kafka Streams state stores | `in_memory`            |          ✅           |
| `STATE_STORE_ROCKS_DB_COMPACTION_STYLE` | Compaction style to use for RocksDB state stores                            | -                      |          ❌           |
| `STATE_STORE_ROCKS_DB_COMPRESSION_TYPE` | Compression type to use for RocksDB state stores                            | -                      |          ❌           |  
| `QUARKUS_DATASOURCE_DB_KIND`            | The database type                                                           | `postgresql`           |          ✅           |
| `QUARKUS_DATASOURCE_JDBC_URL`           | The database JDBC URL                                                       | -                      |          ✅           |
| `QUARKUS_DATASOURCE_USERNAME`           | The database username                                                       | -                      |          ✅           |
| `QUARKUS_DATASOURCE_PASSWORD`           | The database password                                                       | -                      |          ✅           | 
| `QUARKUS_LOG_CONSOLE_JSON`              | Enable logging in JSON format                                               | `false`                |          ❌           |
| `SCANNER_INTERNAL_ENABLED`              | Enable the internal vulnerability scanner                                   | `true`                 |          ❌           |
| `SCANNER_OSSINDEX_ENABLED`              | Enable the OSS Index vulnerability scanner                                  | `true`                 |          ❌           |
| `SCANNER_OSSINDEX_API_USERNAME`         | OSS Index API username                                                      | -                      |          ❌           |
| `SCANNER_OSSINDEX_API_TOKEN`            | OSS Index API token                                                         | -                      |          ❌           |
| `SCANNER_OSSINDEX_BATCH_INTERVAL`       | Max time to wait before submitting incomplete batches                       | `5S`                   |          ❌           |
| `SCANNER_OSSINDEX_ALIAS_SYNC_ENABLED`   | Enable alias syncing for OSS Index                                          | `false`                |          ❌           |
| `SCANNER_SNYK_ENABLED`                  | Enable the Snyk vulnerability scanner                                       | `false`                |          ❌           |
| `SCANNER_SNYK_API_ORG_ID`               | Snyk organization ID                                                        | -                      | When Snyk is enabled |
| `SCANNER_SNYK_API_TOKENS`               | Comma-separated list of Snyk API tokens                                     | -                      | When Snyk is enabled |
| `SCANNER_SNYK_API_VERSION`              | Version of the Snyk API to use                                              | `2022-12-15`           | When Snyk is enabled |
| `SCANNER_SNYK_SEVERITY_SOURCE_PRIORITY` | Priority of preferred source for vulnerability severities                   | `nvd,snyk,redhat,suse` | When Snyk is enabled |
| `SCANNER_SNYK_BATCH_INTERVAL`           | Max time to wait before submitting incomplete batches                       | `5S`                   | When Snyk is enabled |
| `SCANNER_SNYK_BATCH_SIZE`               | Max size of batch at which it will be submitted                             | `100`                  | When Snyk is enabled |
| `SCANNER_SNYK_ALIAS_SYNC_ENABLED`       | Enable alias syncing for Snyk                                               | `false`                |          ❌           |

> **Note**  
> Refer to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/vulnerability-analyzer/src/main/resources/application.properties)
> for a complete overview of available config options.

### Mirror Service

| Environment Variable                          | Description                                | Default          | Required |
|:----------------------------------------------|:-------------------------------------------|:-----------------|:--------:|
| `KAFKA_BOOTSTRAP_SERVERS`                     | Comma-separated list of Kafka servers      | `localhost:9092` |    ✅     |
| `KAFKA_SSL_ENABLED`                           | SSL enabled for using kafka broker         | `false`          |    ❌     |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`            | Number of Kafka Streams threads            | `3`              |    ❌     |
| `MIRROR_DATASOURCE_GITHUB_ALIAS_SYNC_ENABLED` | Enable alias syncing for GitHub Advisories | `false`          |    ❌     |
| `MIRROR_DATASOURCE_OSV_ALIAS_SYNC_ENABLED`    | Enable alias syncing for OSV               | `false`          |    ❌     |
| `QUARKUS_LOG_CONSOLE_JSON`                    | Enable logging in JSON format              | `false`          |    ❌     |

> **Note**  
> Refer to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/mirror-service/src/main/resources/application.properties)
> for a complete overview of available config options.

[Quarkus docs]: https://quarkus.io/guides/config-reference#configuration-sources
