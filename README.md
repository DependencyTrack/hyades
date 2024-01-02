# Hyades

[![Build Status](https://github.com/DependencyTrack/hyades/actions/workflows/ci.yml/badge.svg)](https://github.com/DependencyTrack/hyades/actions/workflows/ci.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=coverage)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)

## What is this? 🤔

Hyades, named after [the star cluster closest to earth](https://en.wikipedia.org/wiki/Hyades_(star_cluster)), 
is an incubating project for decoupling responsibilities from [Dependency-Track]'s monolithic API server into separate, 
scalable™ services. We're using [Apache Kafka] (or Kafka-compatible brokers like [Redpanda]) for communicating between
API server and Hyades services.

If you're interested in the technical background of this project, please refer to 👉 [`WTF.md`](WTF.md) 👈.

The main objectives of Hyades are:

* Enable Dependency-Track to handle portfolios spanning hundreds of thousands of projects
* Improve resilience of Dependency-Track, providing more confidence when relying on it in critical workflows
* Improve deployment and configuration management experience for containerized / cloud native tech stacks

Other than separating responsibilities, the API server has been modified to allow for high availability (active-active)
deployments. Various "hot paths", like [processing of uploaded BOMs](https://github.com/DependencyTrack/hyades-apiserver/pull/218),
have been optimized in the existing code. Further optimization is an ongoing effort.

Hyades already is a *superset* of Dependency-Track, as changes up to Dependency-Track v4.9.1 were ported,
and features made possible by the new architecture have been implemented on top. Where possible, improvements
made in Hyades are, or will be, backported to Dependency-Track v4.x.

## Features

Generally, Hyades can do [everything Dependency-Track can do](https://github.com/DependencyTrack/dependency-track#features).

On top of that, it is capable of:

* Evaluating policies defined in the [Common Expression Language](https://dependencytrack.github.io/hyades/latest/usage/policy-compliance/expressions/) (CEL)
* Verifying the integrity of components, based on hashes consumed from BOMs and remote repositories

## Architecture

Rough overview of the architecture:

![Architecture Overview](docs/architecture-overview.png)

Except the mirror service (which is not actively involved in event processing), all services can be scaled up and down,
to and from multiple instances. Despite being written in Java, all services except the API server can optionally be
deployed as self-contained native binaries, offering a lower resource footprint.

To read more about the individual services, refer to their respective `REAMDE.md`:

* [Repository Metadata Analyzer](repository-meta-analyzer/README.md)
* [Vulnerability Analyzer](vulnerability-analyzer/README.md)

## Great, can I try it? 🙌

Yes! We prepared demo setup that you can use to play around with Hyades.  
Check out 👉 [`DEMO.md`](DEMO.md) 👈 for details!

## Deployment 🚢

The recommended way to deploy Hyades is via Helm. Our [chart](./helm-charts/hyades) is not officially published
to any repository yet, so for now you'll have to clone this repository to access it.

The chart does *not* include:

* a database
* a Kafka-compatible broker

Helm charts to deploy Kafka brokers to Kubernetes are provided by both [Strimzi](https://strimzi.io/)
and [Redpanda](https://github.com/redpanda-data/helm-charts).

### External file server
Currently we support providing policy bundles on an external file server that apiserver polls on regular intervals to check if a bundle is posted or an existing bundle has been modified.
If a change is detected, the apiserver knowledge base is updated with the latest etag of the remote bundle and then the same is pulled and parsed to store as a global vulnerability policy.
To test/ simulate this behavior, there is an nginx server bundled in the docker-compose file. <br/>
To start it off you need to run:<br/>
```shell
docker compose --profile vulnerability-cel-policy up
```
**Currently, this nginx server needs the zip to be present in this relative location: ../../bundles, ie, two levels up the hyades directory**. You can place the policy bunde here which will get copied over to nginx's internal dictory once it is up with docker compose.
Then the same will get polled and picked by apiserver for parsing.<br/>
To change this location to something else, you can modify the volumes section of the nginx service in the docker-compose file.

### Minikube

Deploying to a local [Minikube](https://minikube.sigs.k8s.io/docs/) cluster is a great way to get started.

> **Note**  
> For now, database and Kafka broker are deployed using Docker Compose.

1. Start PostgreSQL and Redpanda via Docker Compose
```shell
docker compose up -d
```
2. Start a local Minikube cluster, exposing `NodePort`s for API server (`30080`) and frontend (`30081`)
```shell
minikube start --ports 30080:30080,30081:30081
```
3. Deploy Hyades
```shell
helm install hyades ./helm-charts/hyades \
  -n hyades --create-namespace \
  -f ./helm-charts/hyades/values.yaml \
  -f ./helm-charts/hyades/values-minikube.yaml
```
4. Wait a moment for all deployments to become *ready*
```shell
kubectl -n hyades rollout status deployment \
  --selector 'app.kubernetes.io/instance=hyades' \
  --watch --timeout 3m
```
5. Visit `http://localhost:30081` in your browser to access the frontend

## Monitoring 📊

### Metrics

A basic metrics monitoring stack is provided, consisting of Prometheus and Grafana.  
To start both services, run:

```shell
docker compose --profile monitoring up -d
```

The services will be available locally at the following locations:

* Prometheus: http://localhost:9090
* Grafana: http://localhost:3000

Prometheus is [configured](monitoring/prometheus.yml) to scrape metrics from the following services in a 5s intervals:

* Redpanda Broker
* API Server
* Notification Publisher
* Repository Meta Analyzer
* Vulnerability Analyzer

The Grafana instance will be automatically [provisioned](monitoring/grafana/provisioning) to use Prometheus as
data source. Additionally, dashboards for the following services are automatically set up:

* Redpanda Broker
* API Server
* Vulnerability Analyzer

### Redpanda Console 🐼

The provided `docker-compose.yml` includes an instance of [Redpanda Console](https://github.com/redpanda-data/console)
to aid with gaining insight into what's happening in the message broker. Among many other things, it can be used to
inspect messages inside any given topic.

The console is exposed at `http://127.0.0.1:28080` and does not require authentication. It's intended for local use only.

## Technical Documentation 💻

### Configuration 📝

Refer to the [`Configuration`](https://dependencytrack.github.io/hyades/latest/reference/configuration/) documentation.

### Development

#### Prerequisites

* JDK 17+
* Maven
* Docker

#### Building

```shell
mvn clean install -DskipTests
```

#### Running locally

Running the Hyades services locally requires both a Kafka broker and a database server to be present.
Containers for Redpanda and PostgreSQL can be launched using Docker Compose:

```shell
docker compose up -d
```

To launch individual services execute the `quarkus:dev` Maven goal for the respective module:

```shell
mvn -pl vulnerability-analyzer quarkus:dev
```

Make sure you've [built](#building) the project at least once, otherwise the above command will fail.

> **Note**  
> If you're unfamiliar with Quarkus' Dev Mode, you can read more about it 
> [here](https://quarkus.io/guides/maven-tooling#dev-mode)

### Testing 🤞

#### Unit Testing 🕵️‍♂️

To execute the unit tests for all Hyades modules:

```shell
mvn clean verify
```

#### End-To-End Testing 🧟

> **Note**  
> End-to-end tests are based on container images. The tags of those images are currently hardcoded.
> For the Hyades services, the tags are set to `latest`. If you want to test local changes, you'll have
> to first:
> * Build container images locally
> * Update the tags in [`AbstractE2ET`](https://github.com/DependencyTrack/hyades/blob/main/e2e/src/test/java/org/hyades/e2e/AbstractE2ET.java)

To execute end-to-end tests as part of the build:

```shell
mvn clean verify -Pe2e-all
```

To execute *only* the end-to-end tests:

```shell
mvn -pl e2e clean verify -Pe2e-all
```

[Apache Kafka]: https://kafka.apache.org/
[Dependency-Track]: https://github.com/DependencyTrack/dependency-track
[Redpanda]: https://redpanda.com/
[notifications]: https://docs.dependencytrack.org/integrations/notifications/
