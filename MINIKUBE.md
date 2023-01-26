# Minikube deployment

### Prerequisites
* minikube installation on target machine
* ```shell
  minikube start
    ```
* kubectl installation on target machine
* helm installation on target machine
* ```shell
  docker-compose up
  ```

### Deployment Steps

* ```shell
  cd vulnerability-analyzer
  mvn clean install
  ```

An example deployment.yaml is available in ``deploymentCharts/vulnerability-analyzer/deployment.yaml``.<br/>
This module now has quarkus-helm and quarkus-kubernetes extensions installed so when the project is build using `mvn clean install` it would also create a deployment.yaml inside ./target/helm/kubernetes/<chart-name>/templates/deployment.yaml<br/>
In addition a values.yaml will be created in ./target/helm/kubernetes/<chart-name>/values.yaml. Upon doing `mvn clean install` the values.yaml will contain these values:
```yaml
---
app:
  serviceType: ClusterIP
  image: <local path to image>
  envs:
    KAFKA_BOOTSTRAP_SERVERS: test
    SCANNER_SNYK_ENABLED: "true"
    QUARKUS_DATASOURCE_USERNAME: test
    SCANNER_OSSINDEX_ENABLED: "false"
    QUARKUS_DATASOURCE_JDBC_URL: test
    SCANNER_OSSINDEX_API_TOKEN: test
    SCANNER_OSSINDEX_API_USERNAME: test
    QUARKUS_DATASOURCE_PASSWORD: test
    SCANNER_SNYK_API_ORG_ID: test
    QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS: test
    SCANNER_SNYK_API_TOKENS: test
```
These values can be updated as per requirement. The value populated by default are coming from the application.properties. For example:
```properties
quarkus.helm.values.image-name.property=image
quarkus.helm.values.image-name.value=ghcr.io/dependencytrack/hyades-vulnerability-analyzer:1.0.0-snapshot
```
The sha of the image that has been tested with and works is here: dd6ba6cc67c021e42ece308b42f9d9d0ab0e312eddbbb922a8650181cfa4dd0d . And the database credentials need to be updated to valid db credentials. These updates can be done either manually or by upgrading the helm chart using helm commands. Once these updates are done, you need to navigate to the module directory in the machine and execute the command below:
```shell
helm install vulnerability-analyzer-helm ./target/helm/kubernetes/vulnerability-analyzer
```
This will start off the deployment. You can view it by launching the minikube dashboard:
```shell
minikube dashboard
```

### Testing the minikube deployment
* To send a new event to the dtrack.vuln-analysis.component topic, open http://localhost:28080/topics/dtrack.vuln-analysis.component?o=-1&p=-1&q&s=50#messages
    * Publish a new message by using Actions>> Publish Message
        * An example message value is:
      ```json
      {
      "name": "test3",
      "purl": "pkg:maven/cyclonedx-core-java@7.1.3",
      "group": "g1",
      "uuid": "438232c4-3b43-4c12-ad3c-eae522c6d158",
      "author": "test3"
      }
      ```
        * The corresponding key to set would be 438232c4-3b43-4c12-ad3c-eae522c6d158
    * Once the message is sent, you can go to the dtrack.vuln-analysis.component.purl topic in the redpanda console and would be able to see a corresponding message that has been processed by the vulnerability analyzer that was deployed using minikube
