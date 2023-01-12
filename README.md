# DT-Kafka-POC

[![Build Status](https://github.com/mehab/DTKafkaPOC/actions/workflows/ci.yml/badge.svg)](https://github.com/mehab/DTKafkaPOC/actions/workflows/ci.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mehab_DTKafkaPOC&metric=coverage)](https://sonarcloud.io/summary/new_code?id=mehab_DTKafkaPOC)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=mehab_DTKafkaPOC&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=mehab_DTKafkaPOC)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=mehab_DTKafkaPOC&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=mehab_DTKafkaPOC)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=mehab_DTKafkaPOC&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=mehab_DTKafkaPOC)

## What is this? 🤔

This project is a proof-of-concept for decoupling responsibilities from [Dependency-Track]'s monolithic API server
into separate, scalable™ services. We're using [Kafka] (or Kafka-compatible brokers like [Redpanda]) for communicating 
between API server and the PoC applications.

As of now, the PoC is capable of:

* Performing vulnerability analysis using [OSS Index] and [Snyk]
* Sending [notifications] via all channels supported by the original API server (E-Mail, Webhook, etc.)

We're planning to expand its set of capabilities further by:

* Performing component meta analysis (e.g. fetching latest versions from remote repositories)
* ...

An overview of the architecture can be found [here](https://excalidraw.com/#room=fba0103fa2642574be40,NomXwyHw3jvoy0yr6JxCJw).

## Great, can I try it? 🙌

Yes! We prepared demo setup that you can use to play around with the PoC.  
Check out 👉 [`DEMO.md`](DEMO.md) 👈 for details!

## Technical Documentation 💻

### Configuration 📝

See [`CONFIGURATION.md`](CONFIGURATION.md).

### Encryption Secret

Dependency-Track needs to store credentials for various purposes in its database.  
Those credentials are AES256 encrypted, using a secret key that has historically been automatically generated when
the API server first launches. Now, with multiple services requiring access to the encrypted data, 
it is necessary to share this secret among them.

To generate the secret key, `openssl` may be used:

```shell
openssl rand 32 > secret.key
```

### Development
To develop the application, you need to run:
```shell
docker-compose up
```
to start off the docker containers. And then need to run:
```shell

quarkus dev
```
to run the application in dev mode.

### Testing 🤞

#### Load Testing 🚀

See [`load-tests`](load-tests).

### Monitoring

#### Metrics

A basic metrics monitoring stack is provided, consisting of Prometheus and Grafana.  
To start both services, run:

```shell
docker compose --profile monitoring up -d grafana
```

The services will be available locally at the following locations:

* Prometheus: http://localhost:9090
* Grafana: http://localhost:3000

Prometheus is [configured](monitoring/prometheus.yml) to scrape metrics from the following services in 5s intervals:

* Redpanda Broker
* API Server
* Notification Publisher
* Repository Meta Analyzer
* Vulnerability Analyzer

The Grafana instance will be automatically [provisioned](monitoring/grafana/provisioning) to use Prometheus as
data source. Additionally, dashboards for the following services are automatically set up:

* Redpanda Broker
* API Server

#### Redpanda Console 🐼

The provided `docker-compose.yml` includes an instance of [Redpanda Console](https://github.com/redpanda-data/console)
to aid with gaining insight into what's happening in the message broker. Among many other things, it can be used to
inspect messages inside any given topic:

![Redpanda Console - Messages](.github/images/redpanda-console_messages.png)

The console is exposed at `http://127.0.0.1:28080` and does not require authentication. It's intended for local use only.

[Dependency-Track]: https://github.com/DependencyTrack/dependency-track
[Kafka]: https://kafka.apache.org/
[notifications]: https://docs.dependencytrack.org/integrations/notifications/
[OSS Index]: https://ossindex.sonatype.org/
[Redpanda]: https://redpanda.com/
[Snyk]: https://snyk.io/

#### Deploying vulnerability-analyzer using minkube 
##### Prerequisites
* minikube installation on target machine
* ```shell
  minikube start
    ```
  ```
* kubectl installation on target machine
* helm installation on target machine
* ```shell
  docker-compose up
  ```

##### Deployment Steps

* ```shell
  cd vulnerability-analyzer
  mvn clean install
  ```

An example deployment.yaml is available in ``deploymentCharts/vulnerability-analyzer/deployment.yaml``.<br/>
This module now has quarkus-helm and quarkus-kubernetes extensions installed so when the project is build using `mvn clean install` it would also create a deployment.yaml inside ./target/helm/kubernetes/<chart-name>/templates/deployment.yaml<br/>
In addition a values.yaml will be created in ./target/helm/kubernetes/<chart-name>/values.yaml. Upon doing `mvn clean install` the values.yaml will contain these values:
```yaml
---
app:
  serviceType: ClusterIP
  image: <local path to image>
  envs:
    KAFKA_BOOTSTRAP_SERVERS: test
    SCANNER_SNYK_ENABLED: "true"
    QUARKUS_DATASOURCE_USERNAME: test
    SCANNER_OSSINDEX_ENABLED: "false"
    QUARKUS_DATASOURCE_JDBC_URL: test
    SCANNER_OSSINDEX_API_TOKEN: test
    SCANNER_OSSINDEX_API_USERNAME: test
    QUARKUS_DATASOURCE_PASSWORD: test
    SCANNER_SNYK_API_ORG_ID: test
    QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS: test
    SCANNER_SNYK_API_TOKENS: test
```
These values can be updated as per requirement. The value populated by default are coming from the application.properties. For example:
```properties
quarkus.helm.values.image-name.property=image
quarkus.helm.values.image-name.value=ghcr.io/mehab/vulnerability-analyzer:1.0.0-snapshot
```
The sha of the image that has been tested with and works is here: dd6ba6cc67c021e42ece308b42f9d9d0ab0e312eddbbb922a8650181cfa4dd0d . And the database credentials need to be updated to valid db credentials. These updates can be done either manually or by upgrading the helm chart using helm commands. Once these updates are done, you need to navigate to the module directory in the machine and execute the command below:
```shell
helm install vulnerability-analyzer-helm ./target/helm/kubernetes/vulnerability-analyzer
```
This will start off the deployment. You can view it by launching the minikube dashboard:
```shell
minikube dashboard
```

##### Testing the minikube deployment
* To send a new event to the dtrack.vuln-analysis.component topic, open http://localhost:28080/topics/dtrack.vuln-analysis.component?o=-1&p=-1&q&s=50#messages
  * Publish a new message by using Actions>> Publish Message
    * An example message value is:
    ```json
    {
    "name": "test3",
    "purl": "pkg:maven/cyclonedx-core-java@7.1.3",
    "group": "g1",
    "uuid": "438232c4-3b43-4c12-ad3c-eae522c6d158",
    "author": "test3"
    }
    ```
    * The corresponding key to set would be 438232c4-3b43-4c12-ad3c-eae522c6d158
  * Once the message is sent, you can go to the dtrack.vuln-analysis.component.purl topic in the redpanda console and would be able to see a corresponding message that has been processed by the vulnerability analyzer that was deployed using minikube
