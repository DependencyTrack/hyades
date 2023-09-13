package org.hyades.e2e;

import io.quarkus.restclient.runtime.QuarkusRestClientBuilder;
import org.hyades.apiserver.ApiServerClient;
import org.hyades.apiserver.ApiServerClientHeaderFactory;
import org.hyades.apiserver.model.CreateTeamRequest;
import org.hyades.apiserver.model.Team;
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

import java.net.URI;
import java.util.Optional;
import java.util.Set;

import static org.testcontainers.lifecycle.Startables.deepStart;

public class AbstractE2ET {

    protected static String POSTGRES_IMAGE = "postgres:15-alpine";
    protected static String REDPANDA_IMAGE = "docker.redpanda.com/vectorized/redpanda:v23.2.8";
    protected static String API_SERVER_IMAGE = "ghcr.io/dependencytrack/hyades-apiserver:snapshot";
    protected static String NOTIFICATION_PUBLISHER_IMAGE = "ghcr.io/dependencytrack/hyades-notification-publisher:snapshot";
    protected static String REPO_META_ANALYZER_IMAGE = "ghcr.io/dependencytrack/hyades-repository-meta-analyzer:snapshot";
    protected static String VULN_ANALYZER_IMAGE = "ghcr.io/dependencytrack/hyades-vulnerability-analyzer:snapshot";

    protected Logger logger;
    private Network internalNetwork;
    protected PostgreSQLContainer<?> postgresContainer;
    protected GenericContainer<?> redpandaContainer;
    protected GenericContainer<?> apiServerContainer;
    protected GenericContainer<?> notificationPublisherContainer;
    protected GenericContainer<?> repoMetaAnalyzerContainer;
    protected GenericContainer<?> vulnAnalyzerContainer;
    protected ApiServerClient apiServerClient;

    @BeforeEach
    void beforeEach() throws Exception {
        logger = LoggerFactory.getLogger(getClass());

        internalNetwork = Network.newNetwork();

        postgresContainer = createPostgresContainer();
        redpandaContainer = createRedpandaContainer();
        deepStart(postgresContainer, redpandaContainer).join();

        initializeRedpanda();

        apiServerContainer = createApiServerContainer();
        apiServerContainer.start();

        notificationPublisherContainer = createNotificationPublisherContainer();
        repoMetaAnalyzerContainer = createRepoMetaAnalyzerContainer();
        vulnAnalyzerContainer = createVulnAnalyzerContainer();
        deepStart(notificationPublisherContainer, repoMetaAnalyzerContainer, vulnAnalyzerContainer).join();

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

    @SuppressWarnings("resource")
    private GenericContainer<?> createApiServerContainer() {
        final var container = new GenericContainer<>(DockerImageName.parse(API_SERVER_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("EXTRA_JAVA_OPTIONS", "-Xmx2g")
                .withEnv("SYSTEM_REQUIREMENT_CHECK_ENABLED", "false")
                .withEnv("ALPINE_DATABASE_MODE", "external")
                .withEnv("ALPINE_DATABASE_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("ALPINE_DATABASE_DRIVER", "org.postgresql.Driver")
                .withEnv("ALPINE_DATABASE_USERNAME", "dtrack")
                .withEnv("ALPINE_DATABASE_PASSWORD", "dtrack")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "redpanda:29092")
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
    private GenericContainer<?> createNotificationPublisherContainer() {
        final var container = new GenericContainer<>(DockerImageName.parse(NOTIFICATION_PUBLISHER_IMAGE))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "redpanda:29092")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
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
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "redpanda:29092")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
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
        final ApiServerClient client = new QuarkusRestClientBuilder()
                .baseUri(URI.create("http://localhost:%d".formatted(apiServerContainer.getFirstMappedPort())))
                .build(ApiServerClient.class);

        logger.info("Changing API server admin password");
        client.forcePasswordChange("admin", "admin", "admin123", "admin123");

        logger.info("Authenticating as admin");
        final String bearerToken = client.login("admin", "admin123");
        ApiServerClientHeaderFactory.setBearerToken(bearerToken);

        logger.info("Creating e2e team");
        final Team team = client.createTeam(new CreateTeamRequest("e2e"));

        // TODO: Should assigned permissions be configurable per test case?
        logger.info("Assigning permissions to e2e team");
        for (final String permission : Set.of(
                "BOM_UPLOAD",
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
        ApiServerClientHeaderFactory.setApiKey(team.apiKeys().get(0).key());

        return client;
    }

    @AfterEach
    void afterEach() {
        ApiServerClientHeaderFactory.reset();

        Optional.ofNullable(vulnAnalyzerContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(repoMetaAnalyzerContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(notificationPublisherContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(apiServerContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(redpandaContainer).ifPresent(GenericContainer::stop);
        Optional.ofNullable(postgresContainer).ifPresent(GenericContainer::stop);

        Optional.ofNullable(internalNetwork).ifPresent(Network::close);
    }


}
