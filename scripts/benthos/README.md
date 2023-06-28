# Benthos

[Benthos](https://www.benthos.dev/) configurations for various "boring" stream processing tasks.

## Usage

```shell
benthos -c <CONFIG_FILE>
```

## Use Cases

### Forwarding uploaded BOMs from a Vanilla DT instance to Hyades

Useful when running a "test" Hyades instance alongside a "production" vanilla Dependency-Track instance.

Forwarding BOMs to Hyades allows evaluation of Hyades under close-to-production workloads.

```shell
export HYADES_DST_API_BASE_URL="https://hyades-test.acme.com"
export HYADES_DST_API_KEY="<API_KEY>"

benthos -c bom-forward_vanilla-to-hyades.yml
```

In vanilla DT, configure an alert of type *Outbound Webhook*:

* Group: `BOM_CONSUMED`
* Destination: `http://<BENTHOS_HOST>:4195/dtrack/notification/bom-consumed`

### Forwarding uploaded BOMs from one Hyades instance to another

Useful when running multiple instances of Hyades, and wanting to emulate close-to-production workloads on test instances.

```shell
export HYADES_SRC_API_BASE_URL="https://hyades-prod.acme.com"
export HYADES_SRC_API_KEY="<SRC_API_KEY>"
export HYADES_DST_API_BASE_URL="https://hyades-test.acme.com"
export HYADES_DST_API_KEY="<DST_API_KEY>"

benthos -c bom-forward_hyades-to-hyades.yml
```

In the source Hyades instance, configure an alert of type *Outbound Webhook*:

* Group: `BOM_PROCESSED`
* Destination: `http://<BENTHOS_HOST>:4195/dtrack/notification/bom-processed`
