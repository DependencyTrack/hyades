<!--
  GENERATED. DO NOT EDIT.

  Generated with: -t ./scripts/config-docs.md.peb -o ./docs/reference/configuration/mirror-service.md mirror-service/src/main/resources/application.properties
-->

## Datasource

### mirror.datasource.github.alias-sync-enabled

Defines whether vulnerability aliases should be parsed from GitHub Advisories.  

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
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_GITHUB_ALIAS_SYNC_ENABLED</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.github.api-key

Defines the API key to use for accessing GitHub's GraphQL API.  It is required in order to use the GitHub datasource.  

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
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_GITHUB_API_KEY</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.github.base-url

Defines the URL of the GitHub GraphQL API endpoint.  

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
      <td style="border-width: 0"><code>https://api.github.com/graphql</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_GITHUB_BASE_URL</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.nvd.api-key

Defines the API key to use for accessing the NVD's REST API.  An API key can be requested via the following form: <https://nvd.nist.gov/developers/request-an-api-key>  

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
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_NVD_API_KEY</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.nvd.base-url

Defines the URL of the NVD REST API.  

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
      <td style="border-width: 0"><code>https://services.nvd.nist.gov/rest/json/cves/2.0</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_NVD_BASE_URL</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.nvd.num-threads

Defines the number of threads with which data is being downloaded from the NVD REST API concurrently.  Has no effect unless [`mirror.datasource.nvd.api-key`](#mirrordatasourcenvdapi-key) is provided.  

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
      <td style="border-width: 0"><code>4</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_NVD_NUM_THREADS</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.osv.alias-sync-enabled

Defines whether vulnerability aliases should be parsed from OSV.  

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
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_OSV_ALIAS_SYNC_ENABLED</code></td>
    </tr>
  </tbody>
</table>


---

### mirror.datasource.osv.base-url

Defines the URL of the OSV storage bucket.  

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
      <td style="border-width: 0"><code>https://osv-vulnerabilities.storage.googleapis.com</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_OSV_BASE_URL</code></td>
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
      <td style="border-width: 0"><code>8093</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_HTTP_PORT</code></td>
    </tr>
  </tbody>
</table>




## Kafka

### kafka-streams.commit.interval.ms

Defines the interval in milliseconds at which consumer offsets are committed to the Kafka brokers.  The Kafka default of `30s` has been modified to `5s`.  <br/><br/>  Refer to <https://kafka.apache.org/documentation/#streamsconfigs_commit.interval.ms> for details.  

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

### kafka-streams.num.stream.threads

The number of threads to allocate for stream processing tasks.  Note that Specifying a number higher than the number of input partitions provides no additional benefit,  as excess threads will simply run idle.  <br/><br/>  Refer to <https://kafka.apache.org/documentation/#streamsconfigs_num.stream.threads> for details.  

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

### kafka.max.request.size

Defines the maximum size of a Kafka producer request in bytes.  <br/><br/>  Some messages like Bill of Vulnerabilities can be bigger than the default 1MiB.  Since the size check is performed before records are compressed, this value may need to be increased  even though the compressed value is much smaller. The Kafka default of 1MiB has been raised to 2MiB.  <br/><br/>  Refer to <https://kafka.apache.org/documentation/#producerconfigs_max.request.size> for details.  

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
      <td style="border-width: 0"><code>2097152</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_MAX_REQUEST_SIZE</code></td>
    </tr>
  </tbody>
</table>


---

### kafka.topic.prefix

Defines an optional prefix to assume for all Kafka topics the application  consumes from, or produces to. The prefix will also be prepended to the  application's consumer group ID.  

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
      <td style="border-width: 0"><code>acme-</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_TOPIC_PREFIX</code></td>
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
      <td style="border-width: 0"><code>${kafka.topic.prefix}hyades-mirror-service</code></td>
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




