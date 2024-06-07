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
import org.dependencytrack.apiserver.model.CreateTeamRequest;
import org.dependencytrack.apiserver.model.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
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

    protected static String POSTGRES_IMAGE = "postgres:15-alpine";
    protected static String REDPANDA_IMAGE = "docker.redpanda.com/vectorized/redpanda:v24.1.7";
    protected static String API_SERVER_IMAGE = "ghcr.io/dependencytrack/hyades-apiserver:snapshot";
    protected static String MIRROR_SERVICE_IMAGE = "ghcr.io/dependencytrack/hyades-mirror-service:snapshot";
    protected static String NOTIFICATION_PUBLISHER_IMAGE = "ghcr.io/dependencytrack/hyades-notification-publisher:snapshot";
    protected static String REPO_META_ANALYZER_IMAGE = "ghcr.io/dependencytrack/hyades-repository-meta-analyzer:snapshot";
    protected static String VULN_ANALYZER_IMAGE = "ghcr.io/dependencytrack/hyades-vulnerability-analyzer:snapshot";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Network internalNetwork = Network.newNetwork();
    protected PostgreSQLContainer<?> postgresContainer;
    protected GenericContainer<?> redpandaContainer;
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
        redpandaContainer = createRedpandaContainer();
        deepStart(postgresContainer, redpandaContainer).join();

        initializeRedpanda();

        generateSecretKey();

        apiServerContainer = createApiServerContainer();
        apiServerContainer.start();

        mirrorServiceContainer = createMirrorServiceContainer();
        notificationPublisherContainer = createNotificationPublisherContainer();
        repoMetaAnalyzerContainer = createRepoMetaAnalyzerContainer();
        vulnAnalyzerContainer = createVulnAnalyzerContainer();
        deepStart(
                mirrorServiceContainer,
                notificationPublisherContainer,
                repoMetaAnalyzerContainer,
                vulnAnalyzerContainer
        ).join();

        apiServerClient = initializeApiServerClient();
    }

    @SuppressWarnings("resource")
    private PostgreSQLContainer<?> createPostgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                .withDatabaseName("dtrack")
                .withUsername("dtrack")
                .withPassword("dtrack")
                .withNetworkAliases("postgres")
                .withNetwork(internalNetwork);
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createRedpandaContainer() {
        return new GenericContainer<>(DockerImageName.parse(REDPANDA_IMAGE))
                .withCommand(
                        "redpanda", "start", "--smp", "1", "--mode", "dev-container",
                        "--reserve-memory", "0M", "--memory", "512M", "--overprovisioned",
                        "--kafka-addr", "PLAINTEXT://0.0.0.0:29092",
                        "--advertise-kafka-addr", "PLAINTEXT://redpanda:29092"
                )
                .waitingFor(Wait.forLogMessage(".*Started Kafka API server.*", 1))
                .withNetworkAliases("redpanda")
                .withNetwork(internalNetwork);
    }

    @SuppressWarnings("resource")
    private void initializeRedpanda() {
        new GenericContainer<>(DockerImageName.parse(REDPANDA_IMAGE))
                .withCreateContainerCmdModifier(cmd -> cmd.withUser("0").withEntrypoint("/bin/bash"))
                .withCommand("/tmp/create-topics.sh")
                .withEnv("REDPANDA_BROKERS", "redpanda:29092")
                .withFileSystemBind("../scripts/create-topics.sh", "/tmp/create-topics.sh", BindMode.READ_ONLY)
                .waitingFor(Wait.forLogMessage(".*All topics created successfully.*", 1))
                .withNetwork(internalNetwork)
                .start();
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
    private GenericContainer<?> createApiServerContainer() {
        final var container = new GenericContainer<>(DockerImageName.parse(API_SERVER_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("EXTRA_JAVA_OPTIONS", "-Xmx512m")
                .withEnv("ALPINE_DATABASE_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("ALPINE_DATABASE_USERNAME", "dtrack")
                .withEnv("ALPINE_DATABASE_PASSWORD", "dtrack")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "redpanda:29092")
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
        final var container = new GenericContainer<>(DockerImageName.parse(MIRROR_SERVICE_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "redpanda:29092")
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
        final var container = new GenericContainer<>(DockerImageName.parse(NOTIFICATION_PUBLISHER_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "redpanda:29092")
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
        final var container = new GenericContainer<>(DockerImageName.parse(REPO_META_ANALYZER_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "redpanda:29092")
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
        final var container = new GenericContainer<>(DockerImageName.parse(VULN_ANALYZER_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("JAVA_OPTS", "-Xmx256m")
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "redpanda:29092")
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
        ApiServerAuthInterceptor.setApiKey(team.apiKeys().getFirst().key());

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
        Optional.ofNullable(redpandaContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(postgresContainer).ifPresent(GenericContainer::stop);

        Optional.ofNullable(internalNetwork).ifPresent(Network::close);
    }


}
