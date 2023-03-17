# Hyades

[![Build Status](https://github.com/DependencyTrack/hyades/actions/workflows/ci.yml/badge.svg)](https://github.com/DependencyTrack/hyades/actions/workflows/ci.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=coverage)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dependency-track_hyades&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=dependency-track_hyades)

## What is this? 🤔

Hyades, named after [the star cluster closest to earth](https://en.wikipedia.org/wiki/Hyades_(star_cluster)), 
is a proof-of-concept for decoupling responsibilities from [Dependency-Track]'s monolithic API server into separate, 
scalable™ services. We're using [Kafka] (or Kafka-compatible brokers like [Redpanda]) for communicating between API 
server and Hyades services.

If you're interested in the technical background of this project, please refer to 👉 [`WTF.md`](WTF.md) 👈.

As of now, Hyades is capable of:

* Performing vulnerability analysis using scanners that leverage:
  * Dependency-Track's internal vulnerability database
  * [OSS Index]
  * [Snyk]
* Gathering component metadata (e.g. latest available version) from remote repositories
* Sending [notifications] via all channels supported by the original API server (E-Mail, Webhook, etc.)

Here's a rough overview of the architecture:

![Architecture Overview](docs/architecture-overview.png)

To read more about the individual services, refer to their respective `REAMDE.md`:

* [Repository Metadata Analyzer](repository-meta-analyzer/README.md)
* [Vulnerability Analyzer](vulnerability-analyzer/README.md)

## Great, can I try it? 🙌

Yes! We prepared demo setup that you can use to play around with Hyades.  
Check out 👉 [`DEMO.md`](DEMO.md) 👈 for details!

## Technical Documentation 💻

### Configuration 📝

See [`CONFIGURATION.md`](CONFIGURATION.md).

### Development

#### Prerequisites

* JDK 17+
* Docker

#### Building

```shell
./mvnw clean install -DskipTests
```

#### Running locally

Running the Hyades services locally requires both a Kafka broker and a database server to be present.
Containers for Redpanda and PostgreSQL can be launched using Docker Compose:

```shell
docker compose up -d
```

To launch individual services execute the `quarkus:dev` Maven goal for the respective module:

```shell
./mvnw -pl vulnerability-analyzer quarkus:dev
```

Make sure you've [built](#building) the project at least once, otherwise the above command will fail.

> **Note**  
> If you're unfamiliar with Quarkus' Dev Mode, you can read more about it 
> [here](https://quarkus.io/guides/maven-tooling#dev-mode)

### Testing 🤞

#### Unit Testing 🕵️‍♂️

To execute the unit tests for all Hyades modules:

```shell
./mvnw clean verify
```

#### Load Testing 🚀

See [`load-tests`](load-tests).

### Monitoring 📊

#### Metrics

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
