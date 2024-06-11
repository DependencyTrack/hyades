# Testing

## Introduction

We generally aim for a test coverage of ~80%. This is also true for new code introduced through pull requests.
We value integration tests more than unit tests, and try to avoid using mocks as much as possible.
If reaching the 80% test coverage requires us to write tests that don't really test anything meaningful,
or require loads of mocking, we rather take lower coverage than writing those tests.

We use [Testcontainers], [Wiremock], and [GreenMail] to test how the system interacts with the outside world.

## Unit Tests

### `hyades`

TBD

### `hyades-apiserver`

!!! warning
    To reduce execution time of the test suite in CI, the PostgreSQL Testcontainer is [reused].
    While tables are truncated after each test, sequences (e.g. for `ID` columns) won't be reset.
    As a consequence, you should not assert on IDs of database records.

### `hyades-frontend`

There are currently no unit tests for the frontend.

## Integration Tests

### `hyades`

Integration tests in the `hyades` repository are implemented as [@QuarkusIntegrationTest]. 
As such, they are executed against an actual build artifact (JAR, container, or native executable).

Class names of integration tests are suffixed with `IT` instead of `Test`.

#### Execution

Integration tests can be launched individually through your IDE, or all at once using Maven:

```shell
mvn -pl '!e2e' clean verify failsafe:integration-test -DskipITs=false
```

To limit the test run to specific modules, use `-pl <module>`, for example:

```shell
mvn -pl vulnerability-analyzer clean verify failsafe:integration-test -DskipITs=false
```

#### Execution in CI

In CI, integration tests are executed:

* Against all JARs, as part of the `CI / Test` workflow
* Against native executables, as part of the `CI / Test Native Image` workflow(s)

Both workflows run for pushes and pull requests to the `main` branch.

### `hyades-apiserver`

The API server repository does not currently differentiate between unit- and integration-tests.

### `hyades-frontend`

There are currently no integration tests for the frontend.

## End-to-End Tests

End-to-End tests spin up containers for all services of the system. The test environment is torn down
and rebuilt for every test case. Containers are started and managed using [Testcontainers].

The tests are located in the [`e2e`](https://github.com/DependencyTrack/hyades/tree/main/e2e) module of the
`hyades` repository. Container images used are defined in the
[`AbstractE2ET`](https://github.com/DependencyTrack/hyades/blob/main/e2e/src/test/java/org/dependencytrack/e2e/AbstractE2ET.java)
class.

### Execution

Tests can be launched individually through your IDE, or all at once using Maven:

```shell
mvn -pl e2e clean verify -Pe2e-all
```

To test against local changes:

1. [Build container images](./building.md#containers) for the modified services
2. Update the image tags in `AbstractE2ET` accordingly
3. Run e2e tests as detailed above

### Execution in CI

In CI, end-to-end tests are executed for every push to the `main` branch.

[@QuarkusIntegrationTest]: https://quarkus.io/guides/getting-started-testing#quarkus-integration-test
[GreenMail]: https://greenmail-mail-test.github.io/greenmail/
[Testcontainers]: https://testcontainers.com/
[WireMock]: https://wiremock.org/
[reused]: https://java.testcontainers.org/features/reuse/