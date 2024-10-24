/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.e2e;

import feign.Feign;
import feign.jaxrs3.JAXRS3Contract;
import org.dependencytrack.apiserver.ApiServerAuthInterceptor;
import org.dependencytrack.apiserver.ApiServerClient;
import org.dependencytrack.apiserver.CompositeDecoder;
import org.dependencytrack.apiserver.CompositeEncoder;
import org.dependencytrack.apiserver.model.ApiKey;
import org.dependencytrack.apiserver.model.CreateTeamRequest;
import org.dependencytrack.apiserver.model.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Set;

import static org.testcontainers.lifecycle.Startables.deepStart;

public class AbstractE2ET {

    private enum KafkaProvider {
        APACHE,
        APACHE_NATIVE,
        REDPANDA,
        TANSU
    }

    private static final String HYADES_APISERVER_TAG = Optional.ofNullable(System.getenv("HYADES_APISERVER_TAG")).orElse("local");
    private static final String HYADES_TAG = Optional.ofNullable(System.getenv("HYADES_TAG")).orElse("snapshot");
    private static final String KAFKA_APACHE_TAG = Optional.ofNullable(System.getenv("KAFKA_APACHE_TAG")).orElse("3.8.0");
    private static final String KAFKA_REDPANDA_TAG = Optional.ofNullable(System.getenv("KAFKA_REDPANDA_TAG")).orElse("v24.2.2");
    private static final String KAFKA_TANSU_TAG = Optional.ofNullable(System.getenv("TANSU_TAG")).orElse("latest");
    private static final String POSTGRES_TAG = Optional.ofNullable(System.getenv("POSTGRES_TAG")).orElse("15-alpine");

    private static final DockerImageName KAFKA_APACHE_IMAGE = DockerImageName.parse("apache/kafka").withTag(KAFKA_APACHE_TAG);
    private static final DockerImageName KAFKA_APACHE_NATIVE_IMAGE = DockerImageName.parse("apache/kafka-native").withTag(KAFKA_APACHE_TAG);
    private static final DockerImageName KAFKA_REDPANDA_IMAGE = DockerImageName.parse("docker.redpanda.com/vectorized/redpanda").withTag(KAFKA_REDPANDA_TAG);
    private static final DockerImageName KAFKA_TANSU_IMAGE = DockerImageName.parse("ghcr.io/tansu-io/tansu").withTag(KAFKA_TANSU_TAG);
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres").withTag(POSTGRES_TAG);
    private static final DockerImageName API_SERVER_IMAGE = DockerImageName.parse("ghcr.io/dependencytrack/hyades-apiserver").withTag(HYADES_APISERVER_TAG);
    private static final DockerImageName MIRROR_SERVICE_IMAGE = DockerImageName.parse("ghcr.io/dependencytrack/hyades-mirror-service").withTag(HYADES_TAG);
    private static final DockerImageName NOTIFICATION_PUBLISHER_IMAGE = DockerImageName.parse("ghcr.io/dependencytrack/hyades-notification-publisher").withTag(HYADES_TAG);
    private static final DockerImageName REPO_META_ANALYZER_IMAGE = DockerImageName.parse("ghcr.io/dependencytrack/hyades-repository-meta-analyzer").withTag(HYADES_TAG);
    private static final DockerImageName VULN_ANALYZER_IMAGE = DockerImageName.parse("ghcr.io/dependencytrack/hyades-vulnerability-analyzer").withTag(HYADES_TAG);

    private static final KafkaProvider KAFKA_PROVIDER = Optional.ofNullable(System.getenv("KAFKA_PROVIDER")).map(KafkaProvider::valueOf).orElse(KafkaProvider.APACHE_NATIVE);

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Network internalNetwork = Network.newNetwork();
    protected PostgreSQLContainer<?> postgresContainer;
    protected GenericContainer<?> kafkaContainer;
    protected GenericContainer<?> apiServerContainer;
    protected GenericContainer<?> mirrorServiceContainer;
    protected GenericContainer<?> notificationPublisherContainer;
    protected GenericContainer<?> repoMetaAnalyzerContainer;
    protected GenericContainer<?> vulnAnalyzerContainer;
    protected ApiServerClient apiServerClient;
    private Path secretKeyPath;

    @BeforeEach
    void beforeEach() throws Exception {
        postgresContainer = createPostgresContainer();
        kafkaContainer = switch (KAFKA_PROVIDER) {
            case APACHE, APACHE_NATIVE -> createApacheKafkaContainer();
            case REDPANDA -> createRedpandaContainer();
            case TANSU -> createTansuContainer();
        };
        deepStart(postgresContainer, kafkaContainer).join();

        generateSecretKey();
        runInitializer();

        apiServerContainer = createApiServerContainer();
        mirrorServiceContainer = createMirrorServiceContainer();
        notificationPublisherContainer = createNotificationPublisherContainer();
        repoMetaAnalyzerContainer = createRepoMetaAnalyzerContainer();
        vulnAnalyzerContainer = createVulnAnalyzerContainer();
        deepStart(
                apiServerContainer,
                mirrorServiceContainer,
                notificationPublisherContainer,
                repoMetaAnalyzerContainer,
                vulnAnalyzerContainer
        ).join();

        apiServerClient = initializeApiServerClient();
    }

    @SuppressWarnings("resource")
    private PostgreSQLContainer<?> createPostgresContainer() {
        return new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("dtrack")
                .withUsername("dtrack")
                .withPassword("dtrack")
                .withNetworkAliases("postgres")
                .withNetwork(internalNetwork);
    }

    @SuppressWarnings("resource")
    protected KafkaContainer createApacheKafkaContainer() {
        final DockerImageName imageName = switch (KAFKA_PROVIDER) {
            case APACHE -> KAFKA_APACHE_IMAGE;
            case APACHE_NATIVE -> KAFKA_APACHE_NATIVE_IMAGE;
            default -> throw new IllegalArgumentException();
        };
        return new KafkaContainer(imageName)
                .withNetworkAliases("kafka")
                .withNetwork(internalNetwork);
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createRedpandaContainer() {
        return new GenericContainer<>(KAFKA_REDPANDA_IMAGE)
                .withCommand(
                        "redpanda", "start", "--smp", "1", "--mode", "dev-container",
                        "--reserve-memory", "0M", "--memory", "512M", "--overprovisioned",
                        "--kafka-addr", "PLAINTEXT://0.0.0.0:9093",
                        "--advertise-kafka-addr", "PLAINTEXT://kafka:9093"
                )
                .waitingFor(Wait.forLogMessage(".*Started Kafka API server.*", 1))
                .withNetworkAliases("kafka")
                .withNetwork(internalNetwork);
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createTansuContainer() {
        final var tansuInitContainer = new GenericContainer<>(POSTGRES_IMAGE)
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/bin/bash"))
                .withCommand("-c", """
                        ((psql -d tansu -q -c "select 1 from cluster limit 1" >/dev/null 2>/dev/null) && echo "Already initialized") \
                        || ((psql -c "CREATE DATABASE tansu") && (wget -q -O- https://raw.githubusercontent.com/tansu-io/tansu/refs/heads/main/work-dir/initdb.d/010-schema.sql | psql -d tansu))\
                        """)
                .withEnv("PGHOST", "postgres")
                .withEnv("PGUSER", "dtrack")
                .withEnv("PGPASSWORD", "dtrack")
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
                .withNetwork(internalNetwork)
                .dependsOn(postgresContainer);

        return new GenericContainer<>(KAFKA_TANSU_IMAGE)
                .withCommand(
                        "--kafka-cluster-id", "RvQwrYegSUCkIPkaiAZQlQ",
                        "--kafka-node-id", "1",
                        "--kafka-listener-url", "tcp://0.0.0.0:9093",
                        "--kafka-advertised-listener-url", "tcp://kafka:9093",
                        "--storage-engine", "pg=postgres://dtrack:dtrack@postgres/tansu",
                        "--work-dir", "/data")
                .withExposedPorts(9093)
                .waitingFor(Wait.forListeningPorts(9093))
                .withNetworkAliases("kafka")
                .withNetwork(internalNetwork)
                .dependsOn(tansuInitContainer);
    }

    private void generateSecretKey() throws Exception {
        secretKeyPath = Files.createTempFile(null, null);
        secretKeyPath.toFile().deleteOnExit();

        final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyGen.init(256, random);
        final SecretKey secretKey = keyGen.generateKey();

        Files.write(secretKeyPath, secretKey.getEncoded());
    }

    @SuppressWarnings("resource")
    private void runInitializer() {
        new GenericContainer<>(API_SERVER_IMAGE)
                .withImagePullPolicy("local".equals(API_SERVER_IMAGE.getVersionPart()) ? PullPolicy.defaultPolicy() : PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTIONS", "-Xmx256m -XX:+UseSerialGC -XX:TieredStopAtLevel=1")
                .withEnv("ALPINE_DATABASE_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("ALPINE_DATABASE_USERNAME", "dtrack")
                .withEnv("ALPINE_DATABASE_PASSWORD", "dtrack")
                .withEnv("ALPINE_DATABASE_POOL_ENABLED", "false")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9093")
                .withEnv("INIT_TASKS_ENABLED", "true")
                .withEnv("INIT_TASKS_KAFKA_TOPICS_ENABLED", "true")
                .withEnv("INIT_AND_EXIT", "true")
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("initializer")))
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
                .withNetwork(internalNetwork)
                .start();
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createApiServerContainer() {
        final var container = new GenericContainer<>(API_SERVER_IMAGE)
                .withImagePullPolicy("local".equals(API_SERVER_IMAGE.getVersionPart()) ? PullPolicy.defaultPolicy() : PullPolicy.alwaysPull())
                .withEnv("EXTRA_JAVA_OPTIONS", "-Xmx512m")
                .withEnv("ALPINE_DATABASE_URL", "jdbc:postgresql://postgres:5432/dtrack?reWriteBatchedInserts=true")
                .withEnv("ALPINE_DATABASE_USERNAME", "dtrack")
                .withEnv("ALPINE_DATABASE_PASSWORD", "dtrack")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9093")
                .withEnv("INIT_TASKS_ENABLED", "false")
                .withEnv("ALPINE_SECRET_KEY_PATH", "/var/run/secrets/secret.key")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(secretKeyPath, 444),
                        "/var/run/secrets/secret.key"
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("apiserver")))
                .waitingFor(Wait.forLogMessage(".*Dependency-Track is ready.*", 1))
                .withNetworkAliases("apiserver")
                .withNetwork(internalNetwork)
                .withExposedPorts(8080);
        customizeApiServerContainer(container);
        return container;
    }

    protected void customizeApiServerContainer(final GenericContainer<?> container) {
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createMirrorServiceContainer() {
        final var container = new GenericContainer<>(MIRROR_SERVICE_IMAGE)
                .withImagePullPolicy("local".equals(MIRROR_SERVICE_IMAGE.getVersionPart()) ? PullPolicy.defaultPolicy() : PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9093")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
                .withEnv("SECRET_KEY_PATH", "/var/run/secrets/secret.key")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(secretKeyPath, 444),
                        "/var/run/secrets/secret.key"
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("mirror-service")))
                .withNetworkAliases("mirror-service")
                .withNetwork(internalNetwork)
                .withStartupAttempts(3);
        customizeMirrorServiceContainer(container);
        return container;
    }

    protected void customizeMirrorServiceContainer(final GenericContainer<?> container) {
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createNotificationPublisherContainer() {
        final var container = new GenericContainer<>(NOTIFICATION_PUBLISHER_IMAGE)
                .withImagePullPolicy("local".equals(NOTIFICATION_PUBLISHER_IMAGE.getVersionPart()) ? PullPolicy.defaultPolicy() : PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9093")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
                .withEnv("SECRET_KEY_PATH", "/var/run/secrets/secret.key")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(secretKeyPath, 444),
                        "/var/run/secrets/secret.key"
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("notification-publisher")))
                .withNetworkAliases("notification-publisher")
                .withNetwork(internalNetwork)
                .withStartupAttempts(3);
        customizeNotificationPublisherContainer(container);
        return container;
    }

    protected void customizeNotificationPublisherContainer(final GenericContainer<?> container) {
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createRepoMetaAnalyzerContainer() {
        final var container = new GenericContainer<>(REPO_META_ANALYZER_IMAGE)
                .withImagePullPolicy("local".equals(REPO_META_ANALYZER_IMAGE.getVersionPart()) ? PullPolicy.defaultPolicy() : PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "kafka:9093")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
                .withEnv("SECRET_KEY_PATH", "/var/run/secrets/secret.key")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(secretKeyPath, 444),
                        "/var/run/secrets/secret.key"
                )
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("repo-meta-analyzer")))
                .withNetworkAliases("repo-meta-analyzer")
                .withNetwork(internalNetwork)
                .withStartupAttempts(3);
        customizeRepoMetaAnalyzerContainer(container);
        return container;
    }

    protected void customizeRepoMetaAnalyzerContainer(final GenericContainer<?> container) {
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createVulnAnalyzerContainer() {
        final var container = new GenericContainer<>(VULN_ANALYZER_IMAGE)
                .withImagePullPolicy("local".equals(VULN_ANALYZER_IMAGE.getVersionPart()) ? PullPolicy.defaultPolicy() : PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "kafka:9093")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("vuln-analyzer")))
                .withNetworkAliases("vuln-analyzer")
                .withNetwork(internalNetwork)
                .withStartupAttempts(3);
        customizeVulnAnalyzerContainer(container);
        return container;
    }

    protected void customizeVulnAnalyzerContainer(final GenericContainer<?> container) {
    }

    private ApiServerClient initializeApiServerClient() {
        final ApiServerClient client = Feign.builder()
                .contract(new JAXRS3Contract())
                .decoder(new CompositeDecoder())
                .encoder(new CompositeEncoder())
                .requestInterceptor(new ApiServerAuthInterceptor())
                .target(ApiServerClient.class, "http://localhost:%d".formatted(apiServerContainer.getFirstMappedPort()));

        logger.info("Changing API server admin password");
        client.forcePasswordChange("admin", "admin", "admin123", "admin123");

        logger.info("Authenticating as admin");
        final String bearerToken = client.login("admin", "admin123");
        ApiServerAuthInterceptor.setBearerToken(bearerToken);

        logger.info("Creating e2e team");
        final Team team = client.createTeam(new CreateTeamRequest("e2e"));

        logger.info("Creating API key for e2e team");
        final ApiKey apiKey = client.createApiKey(team.uuid());

        // TODO: Should assigned permissions be configurable per test case?
        logger.info("Assigning permissions to e2e team");
        for (final String permission : Set.of(
                "BOM_UPLOAD",
                "POLICY_MANAGEMENT",
                "PORTFOLIO_MANAGEMENT",
                "PROJECT_CREATION_UPLOAD",
                "SYSTEM_CONFIGURATION",
                "VIEW_PORTFOLIO",
                "VIEW_VULNERABILITY",
                "VULNERABILITY_MANAGEMENT"
        )) {
            client.addPermissionToTeam(team.uuid(), permission);
        }

        logger.info("Authenticating as e2e team");
        ApiServerAuthInterceptor.setApiKey(apiKey.key());

        return client;
    }

    @AfterEach
    void afterEach() {
        ApiServerAuthInterceptor.reset();

        Optional.ofNullable(vulnAnalyzerContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(repoMetaAnalyzerContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(notificationPublisherContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(mirrorServiceContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(apiServerContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(kafkaContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(postgresContainer).ifPresent(GenericContainer::stop);

        Optional.ofNullable(internalNetwork).ifPresent(Network::close);
    }


}
