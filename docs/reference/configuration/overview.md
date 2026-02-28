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
| `DT_KAFKA_TOPIC_PREFIX`   | Prefix for topic names | -       |    ❌     |

### Repository Meta Analyzer

| Environment Variable                                          | Description                                                 | Default          | Required |
|:--------------------------------------------------------------|:------------------------------------------------------------|:-----------------|:--------:|
| `KAFKA_BOOTSTRAP_SERVERS`                                     | Comma-separated list of Kafka servers                       | `localhost:9092` |    ✅     |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`                            | Number of Kafka Streams threads                             | `3`              |    ❌     |
| `KAFKA_STREAMS_EXCEPTION_THRESHOLDS_DESERIALIZATION_COUNT`    | Threshold number of acceptable deserialization errors       | `5`              |    ❌     |
| `KAFKA_STREAMS_EXCEPTION_THRESHOLDS_DESERIALIZATION_INTERVAL` | Interval for threshold of acceptable deserialization errors | `PT30M`          |    ❌     |
| `KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PROCESSING_COUNT`         | Threshold number of acceptable processing errors            | `50`             |    ❌     |
| `KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PROCESSING_INTERVAL`      | Interval for threshold of acceptable processing errors      | `PT30M`          |    ❌     |
| `KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PRODUCTION_COUNT`         | Threshold number of acceptable production errors            | `5`              |    ❌     |
| `KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PRODUCTION_INTERVAL`      | Interval for threshold of acceptable production errors      | `PT30M`          |    ❌     |
| `QUARKUS_DATASOURCE_DB_KIND`                                  | The database type                                           | `postgresql`     |    ✅     |
| `QUARKUS_DATASOURCE_JDBC_URL`                                 | The database JDBC URL                                       | -                |    ✅     |
| `QUARKUS_DATASOURCE_USERNAME`                                 | The database username                                       | -                |    ✅     |
| `QUARKUS_DATASOURCE_PASSWORD`                                 | The database password                                       | -                |    ✅     |
| `QUARKUS_LOG_CONSOLE_JSON`                                    | Enable logging in JSON format                               | `false`          |    ❌     |

> **Note**  
> Refer
>
to [`application.properties`](https://github.com/DependencyTrack/hyades/blob/main/repository-meta-analyzer/src/main/resources/application.properties)
> for a complete overview of available config options.

[Quarkus docs]: https://quarkus.io/guides/config-reference#configuration-sources
