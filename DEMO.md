# Demo

## Setup 💻

All you need is [Docker], [Docker Compose] and a somewhat capable machine.

> **Note**
> A >4 core CPU and >=16GB RAM are recommended for a smooth experience.

1. Start by creating a new directory for this setup:
```shell
mkdir demo
```
2. Clone both *this* repository and that of our [modified Dependency-Track API server] into the directory you just created:
```shell
cd demo
git clone https://github.com/mehab/DTKafkaPOC.git
git clone --branch internal-dt-latest https://github.com/sahibamittal/dependency-track.git
```
  * Alternatively, should you not have Git installed, you can download the repositories [here](https://github.com/mehab/DTKafkaPOC/archive/refs/heads/main.zip)
    and [here](https://github.com/sahibamittal/dependency-track/archive/refs/heads/internal-dt-latest.zip)
3. Pull and build all required container images, and finally start them:
```shell
cd DTKafkaPOC
docker compose --profile demo pull
docker compose --profile demo build
docker compose --profile demo up -d
```
  * Make sure you include the `--profile demo` flag!
  * Building the images may take a few minutes

Once completed, the following services will be available:

| Service                  | URL                    |
|:-------------------------|:-----------------------|
| Dependency-Track         | http://localhost:8080  |
| Redpanda Console         | http://localhost:28080 |
| PostgreSQL               | `localhost:5432`       |
| Redpanda Kafka API       | `localhost:9092`       |

> **Note**
> You'll not need to interact with PostgreSQL or the Kafka API directly to try out the PoC,
> but if you're curious 🕵️ of course you can!

## Testing 🤞

TBD: Login, upload BOM, view messages in Redpanda console, view analysis results in DT, include screenshots

## Scaling up 📈

One of the goals of this PoC is to achieve scalability, remember? Well, we're delighted to report
that there are multiple ways to scale! If you're interested, you can find out more about the parallelism model 
at play [here](https://docs.confluent.io/platform/current/streams/architecture.html#parallelism-model).

### Scaling a single instance 🚀

Arguably the easiest option is to simply increase the number of worker threads used by a service instance.
By modifying the `KAFKASTREAMS_NUM_STREAM_THREADS` environment variable in `docker-compose.yml`, the number of worker
threads can be tweaked.

We set it to `3` per default, but it can be increased to about `12`. Beyond that, there's no benefit anymore.

### Scaling to multiple instances 🚀🚀🚀

Putting more load on a single service instance is not always desirable, so oftentimes simply increasing the replica 
count is the preferable route. In reality this may be done via Kubernetes manifests, but we can do it in Docker Compose, too:

```shell
docker compose --profile demo up -d --scale poc=3
```

[Docker]: https://docs.docker.com/engine/
[Docker Compose]: https://docs.docker.com/compose/install/
[modified Dependency-Track API server]: https://github.com/sahibamittal/dependency-track