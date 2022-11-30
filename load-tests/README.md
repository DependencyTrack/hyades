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
4. Execute the load test, e.g.:
```shell
make test-vuln-analyzer
```

## Development

When using an IDE, [autocompletion](https://k6.io/docs/misc/intellisense/) for `k6` and `xk6-kafka` can be provided via 
TypeScript type definitions. To install them, run:

```shell
npm ci
```

Finally, open the directory in an editor or IDE with TypeScript support.
