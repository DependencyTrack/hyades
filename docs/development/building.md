# Building

## `hyades`

This segment provides build instructions for the `DependencyTrack/hyades` repository.

### JARs

!!! tip
    IntelliJ users can simply execute the `Build` run configuration.

To build JARs for all modules in the repository:

```shell
mvn clean package -DskipTests
```

For application modules, this will produce a [Quarkus fast-jar] in their respective `target` directory.

To only build JARs for specific modules, use Maven's `-pl` flag:

```shell
mvn -pl repository-meta-analyzer clean package -DskipTests
```

!!! note
    If you made changes to shared modules (e.g. `commons`), those changes may not be visible
    to other modules when building specific modules as shown above. Either include the shared
    modules in the `-pl` argument, or run `mvn clean install -DskipTests` beforehand.

### Containers

!!! tip
    IntelliJ users can simply execute the `Build Container Images` run configuration.

To build JARs **and** container images for all modules in the repository:

```shell
mvn clean package \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.additional-tags=local \
  -DskipTests=true
```

As demonstrated before, you can use Maven's `-pl` flag to limit the build to specific modules.

The resulting container images are tagged as:

```
ghcr.io/dependencytrack/hyades-${moduleName}:local
```

For example:

```
ghcr.io/dependencytrack/hyades-repository-metaanalyzer:local
```

## `hyades-apiserver`

This segment provides build instructions for the `DependencyTrack/hyades-apiserver` repository.

### JAR

!!! tip
    IntelliJ users can simply execute the `Build API Server` run configuration.

To build an executable JAR:

```shell
mvn clean package -DskipTests
```

The resulting file is placed in `./apiserver/target` as `dependency-track-apiserver.jar`.  
The JAR ships with an embedded Jetty server, there's no need to deploy it in an application server like Tomcat or WildFly.

### Container

!!! tip
    IntelliJ users can simply execute the `Build API Server Image` run configuration.

Ensure you've built the API server JAR as outlined above.

To build the API server image:

```shell
docker build \
  -t ghcr.io/dependencytrack/hyades-apiserver:local \
  -f ./apiserver/src/main/docker/Dockerfile \
  ./apiserver
```

## `hyades-frontend`

This segment provides build instructions for the `DependencyTrack/hyades-frontend` repository.

### Distribution

To build the frontend using webpack:

```shell
npm run build
```

The build artifacts are placed in `./dist`.  
The contents of `dist` can be deployed to any webserver capable of serving static files.

### Container

Ensure you've built the frontend as outlined above.

To build the frontend image:

```shell
docker build -f docker/Dockerfile.alpine -t ghcr.io/dependencytrack/hyades-frontend:local .
```

[Quarkus fast-jar]: https://quarkus.io/guides/maven-tooling#fast-jar