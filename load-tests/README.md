# Load Tests

Load test suite based on [`k6`](https://github.com/grafana/k6), [`xk6`](https://github.com/grafana/xk6),
and [`xk6-kafka`](https://github.com/mostafa/xk6-kafka).

## Prerequisites

* UNIX-based operating system
* Go (Required, for installing `xk6` and `xk6-kafka`)
* `make` (Optional, for running predefined commands)
* NPM (Optional, for `k6` and `xk6-kafka` type definitions)

## Usage

1. Install `xk6`, and build a `k6` binary with `xk6-kafka` extension:
```shell
make install
```
2. Verify that the `k6` binary has successfully been created:
```shell
./bin/k6 version

# Example output:
# k6 v0.41.0 ((devel), go1.19.2, linux/amd64)
```
3. Start the services you want to load test, e.g.:
```shell
docker compose -f ../docker-compose.yml up -d postgres redpanda redpanda-init vuln-analyzer
```
4. Complete the [local test environment](#local-test-environment) setup
5. Execute the load test, e.g.:
```shell
make test-vuln-analyzer
```

## Local Test Environment

1. Open a terminal session in the repository's root directory
2. Activate the Docker Compose `load-test` [profile](https://docs.docker.com/compose/profiles/#enabling-profiles):
```shell
source ./scripts/profile-loadtest.sh
```
3. Pull all required container images and start the services:
```shell
docker compose pull
docker compose up -d
```
4. Open Grafana (http://localhost:3000) and login (credentials: `admin` / `admin`)
5. Open the *Vulnerability Analyzer* dashboard and verify that metrics are displayed
6. Start load testing (see [usage](#usage))

To scale instances of the `vuln-analyzer` service, run:

```shell
docker compose up -d --scale vuln-analyzer=<NUMBER_OF_INSTANCES>
```

## Development

When using an IDE, [autocompletion](https://k6.io/docs/misc/intellisense/) for `k6` and `xk6-kafka` can be provided via
TypeScript type definitions. To install them, run:

```shell
npm ci
```

Finally, open the directory in an editor or IDE with TypeScript support.
