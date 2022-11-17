# Configuration

All available configuration options used by this application are listed in [`application.properties`].  
Options can be provided via environment variables as well, refer to the [Quarkus docs] for details.  

Not all options are supposed to be tweaked by users though. This document contains an overview of all
options that are expected to be changed by users.

| Environment Variable                          | Description | Default          |       Required       |
|:----------------------------------------------|:------------|:-----------------|:--------------------:|
| `QUARKUS_KAFKAS_TREAMS_BOOTSTRAP_SERVERS`     |             | `localhost:9092` |          ✅           |
| `KAFKA_STREAMS_NUM_STREAM_THREADS`            |             | `3`              |          ❌           |
| `SCANNER_OSSINDEX_ENABLED`                    |             | `true`           |          ❌           |
| `SCANNER_OSSINDEX_API_USERNAME`               |             | -                |          ❌           |
| `SCANNER_OSSINDEX_API_TOKEN`                  |             | -                |          ❌           |
| `SCANNER_SNYK_ENABLED`                        |             | `false`          |          ❌           |
| `SCANNER_SNYK_ORG_ID`                         |             | -                | When Snyk is enabled |
| `SCANNER_SNYK_TOKEN`                          |             | -                | When Snyk is enabled |
| `SCANNER_SNYK_RATELIMIT_TIMEOUT_DURATION`     |             | `60S`            | When Snyk is enabled |
| `SCANNER_SNYK_RATELIMIT_LIMIT_FOR_PERIOD`     |             | `1500`           | When Snyk is enabled |
| `SCANNER_SNYK_RATELIMIT_LIMIT_REFRESH_PERIOD` |             | `60S`            | When Snyk is enabled |

[`application.properties`]: src/main/resources/application.properties
[Quarkus docs]: https://quarkus.io/guides/config-reference#configuration-sources
