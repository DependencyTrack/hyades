# Overview

Want to hack on Hyades, the upcoming Dependency-Track v5?  
Awesome, here's what you need to know to get started!

!!! abstract "Important"
    Please be sure to read [`CONTRIBUTING.md`](https://github.com/DependencyTrack/hyades/CONTRIBUTING.md) and
    [`CODE_OF_CONDUCT.md`](https://github.com/DependencyTrack/hyades/CODE_OF_CONDUCT.md) as well.

## Repositories

The project consists of the following repositories:

| Repository                                                                     | Description                                                                                                                                           |
|:-------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------|
| [DependencyTrack/hyades](https://github.com/DependencyTrack/hyades)            | Main repository. Includes Hyades services, end-to-end tests, documentation, and deployment manifests. GitHub issues and discussions are managed here. |
| [DependencyTrack/hyades-apiserver](https://github.com/DependencyTrack/hyades)  | Fork of [`DependencyTrack/dependency-track`](https://github.com/DependencyTrack/dependency-track). <br/> GitHub issues and discussions are disabled.  |
| [DependencyTrack/hyades-frontend](https://github.com/DependencyTrack/frontend) | Fork of [`DependencyTrack/frontend`](https://github.com/DependencyTrack/frontend). <br/> GitHub issues and discussions are disabled.                  |

!!! note
    The `hyades` and `hyades-apiserver` repositories are split for historical reasons.  
    We are [planning to merge them](https://github.com/DependencyTrack/hyades/issues/932),
    which should result in less overhead and more opportunities for code sharing.

To clone them all:

```bash
git clone https://github.com/DependencyTrack/hyades.git
git clone https://github.com/DependencyTrack/hyades-apiserver.git
git clone https://github.com/DependencyTrack/hyades-frontend.git
```

## Prerequisites

There are a few things you'll need on your journey:

* Java Development Kit<sup>1</sup> >=21 ([Temurin](https://adoptium.net/temurin/releases) distribution recommended)
* [Maven]<sup>1</sup> >=3.9 (comes bundled with IntelliJ and Eclipse)
* NodeJS<sup>2</sup> >=20
* A Java and JavaScript capable editor or IDE of your preference (we recommend IntelliJ<sup>3</sup>)
* [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/)
* [Docker Compose](https://docs.docker.com/compose/) or [Podman Compose](https://github.com/containers/podman-compose)

??? tip
    <sup>1</sup> We recommend [sdkman](https://sdkman.io/) to install Java and Maven. When working in a corporate environment,
    you should obviously prefer the packages provided by your organization.
    
    <sup>2</sup> If you need to juggle multiple NodeJS versions on your system, consider using
    [nvm](https://github.com/nvm-sh/nvm) to make this more bearable.
    
    <sup>3</sup> We provide common [run configurations](https://www.jetbrains.com/help/idea/run-debug-configuration.html) for IntelliJ
    in the `.idea/runConfigurations` directories of each repository for convenience.
    IntelliJ will automatically pick those up when you open this repository.

## Core Technologies

Knowing about the core technologies may help you with understanding the code base.

### Infrastructure

| Technology                                | Purpose               |
|:------------------------------------------|:----------------------|
| [PostgreSQL](https://www.postgresql.org/) | Database              |
| [Apache Kafka](https://kafka.apache.org/) | Messaging / Streaming |

### API Server

| Technology                                                                                  | Purpose                         |
|:--------------------------------------------------------------------------------------------|:--------------------------------|
| [JAX-RS](https://projects.eclipse.org/projects/ee4j.rest)                                   | REST API specification          |
| [Jersey](https://eclipse-ee4j.github.io/jersey/)                                            | JAX-RS implementation           |
| [Java Data Objects (JDO)](https://db.apache.org/jdo/)                                       | Persistence specification       |
| [DataNucleus](https://www.datanucleus.org/products/accessplatform/jdo/getting_started.html) | JDO implementation              |
| [JDBI](https://jdbi.org/#_introduction_to_jdbi_3)                                           | Lightweight database operations |
| [Liquibase]                                                                                 | Database migrations             |
| [Confluent Parallel Consumer](https://github.com/confluentinc/parallel-consumer)            | Kafka message processing        |
| [Jetty](https://www.eclipse.org/jetty/)                                                     | Servlet Container               |
| [Alpine](https://github.com/stevespringett/Alpine)                                          | Framework / Scaffolding         |
| [Maven]                                                                                     | Package manager / Build tool    |
| [Java]                                                                                      | Programming language            |

### Hyades Services

| Technology      | Purpose                      |
|:----------------|:-----------------------------|
| [Kafka Streams] | Stream processing            |
| [Quarkus]       | Framework / Scaffolding      |
| [Maven]         | Package manager / Build tool |
| [Java]          | Programming language         |

### Frontend

| Technology                         | Purpose                      |
|:-----------------------------------|:-----------------------------|
| [webpack](https://webpack.js.org/) | Asset bundler                |
| [Vue.js](https://vuejs.org/)       | Framework                    |
| NPM                                | Package manager / Build tool |
| JavaScript                         | Programming language         |


[embedded Jetty server]: https://github.com/stevespringett/Alpine/tree/master/alpine-executable-war
[GraalVM]: https://www.graalvm.org/
[Java]: https://dev.java/
[JBang]: https://www.jbang.dev/
[Kafka Streams]: https://kafka.apache.org/37/documentation/streams/
[Liquibase]: https://www.liquibase.com/
[Maven]: https://maven.apache.org/
[Quarkus]: https://quarkus.io/