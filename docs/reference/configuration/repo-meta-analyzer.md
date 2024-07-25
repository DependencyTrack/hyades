<!--
  GENERATED. DO NOT EDIT.

  Generated with: --template ./scripts/config-docs.md.peb --output ./docs/reference/configuration/repo-meta-analyzer.md ./repository-meta-analyzer/src/main/resources/application.properties
-->

## Cache

### quarkus.cache.caffeine."metaAnalyzer".expire-after-write

Defines the time-to-live of cache entries.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT2H</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_CACHE_CAFFEINE__METAANALYZER__EXPIRE_AFTER_WRITE</code></td>
    </tr>
  </tbody>
</table>


---

### quarkus.cache.caffeine."metaAnalyzer".initial-capacity

Defines the initial capacity of the cache.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_CACHE_CAFFEINE__METAANALYZER__INITIAL_CAPACITY</code></td>
    </tr>
  </tbody>
</table>


---

### quarkus.cache.enabled

Defines whether caching of analysis results shall be enabled.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>true</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_CACHE_ENABLED</code></td>
    </tr>
  </tbody>
</table>




## Database

### quarkus.datasource.jdbc.url

Specifies the JDBC URL to use when connecting to the database.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_DATASOURCE_JDBC_URL</code></td>
    </tr>
  </tbody>
</table>


---

### quarkus.datasource.password

Specifies the password to use when authenticating to the database.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_DATASOURCE_PASSWORD</code></td>
    </tr>
  </tbody>
</table>


---

### quarkus.datasource.username

Specifies the username to use when authenticating to the database.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_DATASOURCE_USERNAME</code></td>
    </tr>
  </tbody>
</table>




## General

### secret.key.path

Defines the path to the secret key to be used for data encryption and decryption.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>~/.dependency-track/keys/secret.key</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>SECRET_KEY_PATH</code></td>
    </tr>
  </tbody>
</table>




## HTTP

### quarkus.http.port

HTTP port to listen on. Application metrics will be available via this port.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>8091</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_HTTP_PORT</code></td>
    </tr>
  </tbody>
</table>




## Kafka

### dt.kafka.topic.prefix

Defines an optional prefix to assume for all Kafka topics the application  consumes from, or produces to. The prefix will also be prepended to the  application's consumer group ID.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>acme-</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>DT_KAFKA_TOPIC_PREFIX</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.auto.offset.reset

Refer to <https://kafka.apache.org/documentation/#consumerconfigs_auto.offset.reset> for details.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[earliest, latest, none]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>earliest</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_AUTO_OFFSET_RESET</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.commit.interval.ms

Defines the interval in milliseconds at which consumer offsets are committed to the Kafka brokers.  The Kafka default of `30s` has been modified to `5s`.  <br/><br/>  Refer to <https://kafka.apache.org/documentation/#streamsconfigs_commit.interval.ms> for details.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5000</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_COMMIT_INTERVAL_MS</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.exception.thresholds.deserialization.count

Defines the threshold for records failing to be deserialized within [`kafka-streams.exception.thresholds.deserialization.interval`](#kafka-streamsexceptionthresholdsdeserializationinterval).  Deserialization failures within the threshold will be logged, failures exceeding the threshold cause the application  to stop processing further records, and shutting down.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_EXCEPTION_THRESHOLDS_DESERIALIZATION_COUNT</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.exception.thresholds.deserialization.interval

Defines the interval within which up to [`kafka-streams.exception.thresholds.deserialization.count`](#kafka-streamsexceptionthresholdsdeserializationcount) records are  allowed to fail deserialization. Deserialization failures within the threshold will be logged,  failures exceeding the threshold cause the application to stop processing further records, and shutting down.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT30M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_EXCEPTION_THRESHOLDS_DESERIALIZATION_INTERVAL</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.exception.thresholds.processing.count

Defines the threshold for records failing to be processed within [`kafka-streams.exception.thresholds.processing.interval`](#kafka-streamsexceptionthresholdsprocessinginterval).  Processing failures within the threshold will be logged, failures exceeding the threshold cause the application  to stop processing further records, and shutting down.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>50</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PROCESSING_COUNT</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.exception.thresholds.processing.interval

Defines the interval within which up to [`kafka-streams.exception.thresholds.processing.count`](#kafka-streamsexceptionthresholdsprocessingcount) records are  allowed to fail processing. Processing failures within the threshold will be logged,  failures exceeding the threshold cause the application to stop processing further records, and shutting down.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT30M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PROCESSING_INTERVAL</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.exception.thresholds.production.count

Defines the threshold for records failing to be produced within [`kafka-streams.exception.thresholds.production.interval`](#kafka-streamsexceptionthresholdsproductioninterval).  Production failures within the threshold will be logged, failures exceeding the threshold cause the application  to stop processing further records, and shutting down.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>5</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PRODUCTION_COUNT</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.exception.thresholds.production.interval

Defines the interval within which up to [`kafka-streams.exception.thresholds.production.count`](#kafka-streamsexceptionthresholdsproductioncount) records are  allowed to fail producing. Production failures within the threshold will be logged,  failures exceeding the threshold cause the application to stop processing further records, and shutting down.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>duration</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>PT30M</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_EXCEPTION_THRESHOLDS_PRODUCTION_INTERVAL</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.metrics.recording.level

Refer to <https://kafka.apache.org/documentation/#adminclientconfigs_metrics.recording.level> for details.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>enum</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Valid Values</th>
      <td style="border-width: 0"><code>[INFO, DEBUG, TRACE]</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>DEBUG</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_METRICS_RECORDING_LEVEL</code></td>
    </tr>
  </tbody>
</table>


---

### kafka-streams.num.stream.threads

Refer to <https://kafka.apache.org/documentation/#streamsconfigs_num.stream.threads> for details.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>integer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>3</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_STREAMS_NUM_STREAM_THREADS</code></td>
    </tr>
  </tbody>
</table>


---

### kafka.bootstrap.servers

Comma-separated list of brokers to use for establishing the initial connection to the Kafka cluster.  <br/><br/>  Refer to <https://kafka.apache.org/documentation/#consumerconfigs_bootstrap.servers> for details.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">true</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>null</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Example</th>
      <td style="border-width: 0"><code>broker-01.acme.com:9092,broker-02.acme.com:9092</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_BOOTSTRAP_SERVERS</code></td>
    </tr>
  </tbody>
</table>


---

### quarkus.kafka-streams.application-id

Defines the ID to uniquely identify this application in the Kafka cluster.  <br/><br/>  Refer to <https://kafka.apache.org/documentation/#streamsconfigs_application.id> for details.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>string</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>${dt.kafka.topic.prefix}hyades-repository-meta-analyzer</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_STREAMS_APPLICATION_ID</code></td>
    </tr>
  </tbody>
</table>




## Observability

### quarkus.log.console.json

Defines whether logs should be written in JSON format.  

<table>
  <tbody style="border: 0">
    <tr>
      <th style="text-align: right">Required</th>
      <td style="border-width: 0">false</td>
    </tr>
    <tr>
      <th style="text-align: right">Type</th>
      <td style="border-width: 0"><code>boolean</code></td>
    </tr>
    <tr>
      <th style="text-align: right">Default</th>
      <td style="border-width: 0"><code>false</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_LOG_CONSOLE_JSON</code></td>
    </tr>
  </tbody>
</table>




