# DT-Kafka-POC

Implement the design mentioned here: https://excalidraw.com/#room=fba0103fa2642574be40,NomXwyHw3jvoy0yr6JxCJw

## Setup

### Set config properties:
 
In order to provide configuration to this application, you would need to modify the 
[`application.properties`](src/main/resources/application.properties) file, or export the following environment variables:

- SCANNER_OSSINDEX_ENABLED=true
- SCANNER_OSSINDEX_API_USERNAME=username
- SCANNER_OSSINDEX_API_TOKEN=token
- SCANNER_SNYK_ENABLED=true
- SCANNER_SNYK_ORG_ID=orgid
- SCANNER_SNYK_TOKEN=token
- SCANNER_CACHE_VALIDITY_PERIOD=6666

> **Note**
> The OSS Index analyzer is enabled per default for unauthenticated use.

Refer to the [Quarkus Configuration Reference](https://quarkus.io/guides/config-reference#configuration-sources) 
for more information about available configuration sources and the order in which they're loaded.

### Verify if Cwe store contains values

Get request to http://localhost:8089/cwe/data?id=178

### Testing

The `test.sh` script can be used to test the analyzer.  
It publishes an event to the `component-analysis` topic for each component with a 
PURL in a given CycloneDX BOM, thus emulating the API server after a BOM was uploaded to it.

```shell
./test.sh bom.json
```

> **Note**
> The script requires `jq` and `rpk` to be installed.  
> They can be installed using Homebrew:
> ```shell
> brew install jq
> brew install redpanda-data/tap/redpanda
> ```

Analysis results that are intended for consumption by the API server will be
published to the `component-vuln-analysis-result` topic.

## Redpanda Console

The provided `docker-compose.yml` includes an instance of [Redpanda Console](https://github.com/redpanda-data/console)
to aid with gaining insight into what's happening in the message broker. Among many other things, it can be used to
inspect messages inside any given topic:

![Redpanda Console - Messages](.github/images/redpanda-console_messages.png)

The console is exposed at `http://127.0.0.1:28080` and does not require authentication. It's intended for local use only.

## Running this poc with Dependency Track
Follow the below steps to run this poc in integration with dependency track for an end to end test. Currently, we have been able to implement sending of events from DT to a topic which is being polled by this poc application. 
- Use this branch of dependency track backend server from git: https://github.com/sahibamittal/dependency-track/tree/internal-dt-latest
- Run the dependency track api server using provided maven <b>jetty profile</b>.
- Use this branch of dependency track front end from git: `https://github.com/sahibamittal/dependency-track-frontend/tree/internal-dt-latest`
- Run the frontend using `npm ci` and then `npm run serve`
- Run the containers for the poc code using `docker compose up`
- Run the poc code using: `quarkus dev -Dalpine.application.properties=./alpine.properties`
- Run redpanda console on `http://localhost:28080`
- Login to dependency track application running locally on the port pointed by the front end code
- Create a project and load an sbom into dependency track
- You should be able to see events appearing in the event new topic in the redpanda console. You should also be able to see logs in the quarkus application as it analyzes the components on the posted bom components in the topic. The latter would be based on the configuration you used to start the poc application.
