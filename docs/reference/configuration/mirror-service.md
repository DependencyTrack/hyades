<!--
  GENERATED. DO NOT EDIT.

  Generated with: -t scripts/config-docs.md.peb -o docs/reference/configuration/mirror-service.md mirror-service/src/main/resources/application.properties
  Generated on:   2024-03-25T16:14:25.343118970+01:00[Europe/Berlin]
-->

## General

### quarkus.http.port



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



## GitHub Datasource

### mirror.datasource.github.alias-sync-enabled



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

### mirror.datasource.github.api-key



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
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_GITHUB_API_KEY</code></td>
    </tr>
  </tbody>
</table>

### mirror.datasource.github.base-url



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
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_GITHUB_BASE_URL</code></td>
    </tr>
  </tbody>
</table>



## Kafka

### kafka-streams.commit.interval.ms



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

### kafka-streams.exception.thresholds.deserialization.count



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

### kafka-streams.exception.thresholds.deserialization.interval



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

### kafka-streams.exception.thresholds.processing.count



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

### kafka-streams.exception.thresholds.processing.interval



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

### kafka-streams.exception.thresholds.production.count



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

### kafka-streams.exception.thresholds.production.interval



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

### kafka-streams.num.stream.threads



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

### kafka.topic.prefix



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
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>KAFKA_TOPIC_PREFIX</code></td>
    </tr>
  </tbody>
</table>

### quarkus.kafka.devservices.image-name

hidden  

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
      <td style="border-width: 0"><code>docker.redpanda.com/vectorized/redpanda:v23.3.6</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_DEVSERVICES_IMAGE_NAME</code></td>
    </tr>
  </tbody>
</table>

### quarkus.kafka.devservices.topic-partitions."dtrack.notification.datasource-mirroring"

hidden  

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
      <td style="border-width: 0"><code>1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_DEVSERVICES_TOPIC_PARTITIONS__DTRACK_NOTIFICATION_DATASOURCE_MIRRORING_</code></td>
    </tr>
  </tbody>
</table>

### quarkus.kafka.devservices.topic-partitions."dtrack.vulnerability"

hidden  

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
      <td style="border-width: 0"><code>1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_DEVSERVICES_TOPIC_PARTITIONS__DTRACK_VULNERABILITY_</code></td>
    </tr>
  </tbody>
</table>

### quarkus.kafka.devservices.topic-partitions."dtrack.vulnerability.digest"

hidden  

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
      <td style="border-width: 0"><code>1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_DEVSERVICES_TOPIC_PARTITIONS__DTRACK_VULNERABILITY_DIGEST_</code></td>
    </tr>
  </tbody>
</table>

### quarkus.kafka.devservices.topic-partitions."dtrack.vulnerability.mirror.command"

hidden  

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
      <td style="border-width: 0"><code>1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_DEVSERVICES_TOPIC_PARTITIONS__DTRACK_VULNERABILITY_MIRROR_COMMAND_</code></td>
    </tr>
  </tbody>
</table>

### quarkus.kafka.devservices.topic-partitions."dtrack.vulnerability.mirror.state"

hidden  

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
      <td style="border-width: 0"><code>1</code></td>
    </tr>
    <tr>
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>QUARKUS_KAFKA_DEVSERVICES_TOPIC_PARTITIONS__DTRACK_VULNERABILITY_MIRROR_STATE_</code></td>
    </tr>
  </tbody>
</table>



## NVD Datasource

### mirror.datasource.nvd.api-key



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
      <th style="text-align: right">ENV</th>
      <td style="border-width: 0"><code>MIRROR_DATASOURCE_NVD_API_KEY</code></td>
    </tr>
  </tbody>
</table>

### mirror.datasource.nvd.num-threads



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



## OSV Datasource

### mirror.datasource.osv.alias-sync-enabled



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

### mirror.datasource.osv.base-url



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



## Observability

### quarkus.log.console.json



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


