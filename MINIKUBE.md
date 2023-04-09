# Minikube/ Openshift deployment

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
* For openshift deployment, one should have a running cluster on openshift and access to the same.
* 
### Deployment Steps for Minikube

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
    QUARKUS_DATASOURCE_JDBC_URL: test
    QUARKUS_LOG_CATEGORY_ORG_APACHE_KAFKA_LEVEL: WARN
    SCANNER_SNYK_API_VERSION: 2023-03-29~beta
    KAFKA_BOOTSTRAP_SERVERS: test
    SCANNER_SNYK_ENABLED: "true"
    SCANNER_SNYK_API_BASE_URL: https://api.snyk.io
    QUARKUS_DATASOURCE_USERNAME: test
    SCANNER_OSSINDEX_ENABLED: "false"
    SCANNER_OSSINDEX_API_TOKEN: test
    SCANNER_OSSINDEX_API_USERNAME: test
    QUARKUS_DATASOURCE_PASSWORD: test
    SCANNER_OSSINDEX_API_BASE_URL: https://ossindex.sonatype.org
    SCANNER_SNYK_API_ORG_ID: test
    KAFKA_SSL_ENABLED: "false"
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
* An end to end test can be conducted by running *docker compose up* on hyades project and then turning on the hyades-apiserver, frontend and for example the vulnerability-analyzer from the minikube deployment.
* Once a component is added to a project in the frontend then the component sent for scanning should also be visible on *dtrack.vuln-analysis.component* topic. 
* Once the analysis is complete, the result would be visible on *dtrack.vuln-analysis.result* topic and the frontend as well.


### Deployment Steps for OpenShift
* The deployment.yaml generated in running ```mvn clean install``` in the above minikube deployment can be used for an openshift based deployment as well.