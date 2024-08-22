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
mvn -pl mirror-service,vulnerability-analyzer clean package -DskipTests
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
ghcr.io/dependencytrack/hyades-vulnerability-analyzer:local
```

### Native Executables

!!! tip
    IntelliJ users can simply execute the `Build Native` run configuration.

Application modules in the `hyades` repository can be compiled to [native executables], by leveraging [GraalVM Native Image].

!!! warning
    Building native executables is resource intensive and can take a few minutes to complete.

!!! note
    Building native executables requires *GraalVM for JDK 21* or newer.  
    You can install GraalVM with `sdkman`: <https://sdkman.io/jdks#graalce>

To build native executables for all modules:

```shell
export GRAALVM_HOME="$(sdk home java 21.0.2-graalce)"
mvn clean package -Dnative -DskipTests
```

As demonstrated before, you can use Maven's `-pl` flag to limit the build to specific modules.

If installing GraalVM is not possible, or you want to use the native executables in a container,
but your host system is *not* Linux, you can leverage a GraalVM container to perform the build:

```shell
mvn clean package -Dnative -DskipTests -Dquarkus.native.container-build=true
```

## `hyades-apiserver`

This segment provides build instructions for the `DependencyTrack/hyades-apiserver` repository.

### JAR

!!! tip
    IntelliJ users can simply execute the `Build API Server` run configuration.

To build an executable JAR:

```shell
mvn clean package \
  -Pclean-exclude-wars \
  -Penhance \
  -Pembedded-jetty \
  -DskipTests \
  -Dlogback.configuration.file=src/main/docker/logback.xml
```

The resulting file is placed in `./target` as `dependency-track-apiserver.jar`.  
The JAR ships with an embedded Jetty server, there's no need to deploy it in an application server like Tomcat or WildFly.

### Container

!!! tip
    IntelliJ users can simply execute the `Build API Server Image` run configuration.

Ensure you've built the API server JAR as outlined above.

To build the API server image:

```shell
docker build \
  --build-arg WAR_FILENAME=dependency-track-apiserver.jar \
  -t ghcr.io/dependencytrack/hyades-apiserver:local \
  -f ./src/main/docker/Dockerfile \
  .
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

[GraalVM Native Image]: https://www.graalvm.org/latest/reference-manual/native-image/
[Quarkus fast-jar]: https://quarkus.io/guides/maven-tooling#fast-jar
[native executables]: https://quarkus.io/guides/native-reference