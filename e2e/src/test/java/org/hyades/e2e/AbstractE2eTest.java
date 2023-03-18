package org.hyades.e2e;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.hyades.apiserver.ApiServerClient;
import org.hyades.apiserver.ApiServerClientHeaderFactory;
import org.hyades.apiserver.model.CreateTeamRequest;
import org.hyades.apiserver.model.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.Set;

import static org.testcontainers.lifecycle.Startables.deepStart;

public class AbstractE2eTest {

    protected Logger logger;
    private Network internalNetwork;
    protected PostgreSQLContainer<?> postgresContainer;
    protected GenericContainer<?> redpandaContainer;
    protected GenericContainer<?> apiServerContainer;
    protected GenericContainer<?> repoMetaAnalyzerContainer;
    protected GenericContainer<?> vulnAnalyzerContainer;
    protected ApiServerClient apiServerClient;

    @BeforeEach
    void beforeEach() {
        logger = LoggerFactory.getLogger(getClass());

        internalNetwork = Network.newNetwork();

        postgresContainer = createPostgresContainer();
        redpandaContainer = createRedpandaContainer();
        deepStart(postgresContainer, redpandaContainer).join();

        initializeRedpanda();

        apiServerContainer = createApiServerContainer();
        apiServerContainer.start();

        repoMetaAnalyzerContainer = createRepoMetaAnalyzerContainer();
        vulnAnalyzerContainer = createVulnAnalyzerContainer();
        deepStart(repoMetaAnalyzerContainer, vulnAnalyzerContainer);

        apiServerClient = initializeApiServerClient();
    }

    private PostgreSQLContainer<?> createPostgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("dtrack")
                .withUsername("dtrack")
                .withPassword("dtrack")
                .withNetworkAliases("postgres")
                .withNetwork(internalNetwork);
    }

    private GenericContainer<?> createRedpandaContainer() {
        return new GenericContainer<>(DockerImageName.parse("docker.redpanda.com/vectorized/redpanda:v22.3.10"))
                .withCommand(
                        "redpanda", "start", "--mode", "dev-container",
                        "--kafka-addr", "PLAINTEXT://0.0.0.0:29092",
                        "--advertise-kafka-addr", "PLAINTEXT://redpanda:29092"
                )
                .waitingFor(Wait.forLogMessage(".*Started Kafka API server.*", 1))
                .withNetworkAliases("redpanda")
                .withExposedPorts(9092)
                .withNetwork(internalNetwork);
    }

    private void initializeRedpanda() {
        new GenericContainer<>(DockerImageName.parse("docker.redpanda.com/vectorized/redpanda:v22.3.10"))
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/bin/bash"))
                .withCommand("/tmp/create-topics.sh")
                .withEnv("REDPANDA_BROKERS", "redpanda:29092")
                .withFileSystemBind("../scripts/create-topics.sh", "/tmp/create-topics.sh")
                .waitingFor(Wait.forLogMessage(".*All topics created successfully.*", 1))
                .withNetwork(internalNetwork)
                .start();
    }

    private GenericContainer<?> createApiServerContainer() {
        return new GenericContainer<>(DockerImageName.parse("ghcr.io/dependencytrack/hyades-apiserver:snapshot"))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("ALPINE_DATABASE_MODE", "external")
                .withEnv("ALPINE_DATABASE_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("ALPINE_DATABASE_DRIVER", "org.postgresql.Driver")
                .withEnv("ALPINE_DATABASE_USERNAME", "dtrack")
                .withEnv("ALPINE_DATABASE_PASSWORD", "dtrack")
                .withEnv("KAFKA_BOOTSTRAP_SERVERS", "redpanda:29092")
                // .withLogConsumer(new Slf4jLogConsumer(logger))
                .waitingFor(Wait.forLogMessage(".*Dependency-Track is ready.*", 1))
                .withNetworkAliases("apiserver")
                .withNetwork(internalNetwork)
                .withExposedPorts(8080);
    }

    private GenericContainer<?> createRepoMetaAnalyzerContainer() {
        return new GenericContainer<>(DockerImageName.parse("ghcr.io/dependencytrack/hyades-repository-meta-analyzer:latest-native"))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "redpanda:29092")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
                // .withLogConsumer(new Slf4jLogConsumer(logger))
                .withNetworkAliases("repo-meta-analyzer")
                .withNetwork(internalNetwork)
                .withStartupAttempts(3);
    }

    private GenericContainer<?> createVulnAnalyzerContainer() {
        final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("ghcr.io/dependencytrack/hyades-vulnerability-analyzer:latest-native"))
                .withImagePullPolicy(PullPolicy.alwaysPull())
                .withEnv("QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS", "redpanda:29092")
                .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://postgres:5432/dtrack")
                .withEnv("QUARKUS_DATASOURCE_USERNAME", "dtrack")
                .withEnv("QUARKUS_DATASOURCE_PASSWORD", "dtrack")
                // .withLogConsumer(new Slf4jLogConsumer(logger))
                .withNetworkAliases("vuln-analyzer")
                .withNetwork(internalNetwork)
                .withStartupAttempts(3);
        customizeVulnAnalyzerContainer(container);
        return container;
    }

    protected void customizeVulnAnalyzerContainer(final GenericContainer<?> container) {
    }

    private ApiServerClient initializeApiServerClient() {
        final ApiServerClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:%d".formatted(apiServerContainer.getFirstMappedPort())))
                .register(new JacksonJsonProvider(), 1)
                // .proxyAddress("localhost", 8083)
                .build(ApiServerClient.class);

        logger.info("Changing API server admin password");
        client.forcePasswordChange("admin", "admin", "admin123", "admin123");

        logger.info("Authenticating as admin");
        ApiServerClientHeaderFactory.bearerToken = client.login("admin", "admin123");

        logger.info("Creating e2e team");
        final Team team = client.createTeam(new CreateTeamRequest("e2e"));

        logger.info("Assigning permissions to e2e team");
        for (final String permission : Set.of(
                "VIEW_PORTFOLIO",
                "VIEW_VULNERABILITY",
                "BOM_UPLOAD",
                "PROJECT_CREATION_UPLOAD",
                "PORTFOLIO_MANAGEMENT",
                "VULNERABILITY_MANAGEMENT"
        )) {
            client.addPermissionToTeam(team.uuid(), permission);
        }

        ApiServerClientHeaderFactory.apiKey = team.apiKeys().get(0).key();

        return client;
    }

    @AfterEach
    void afterEach() {
        ApiServerClientHeaderFactory.reset();

        if (postgresContainer != null) {
            postgresContainer.stop();
        }

        if (redpandaContainer != null) {
            redpandaContainer.stop();
        }

        if (apiServerContainer != null) {
            apiServerContainer.stop();
        }

        if (repoMetaAnalyzerContainer != null) {
            repoMetaAnalyzerContainer.close();
        }

        if (vulnAnalyzerContainer != null) {
            vulnAnalyzerContainer.stop();
        }
    }


}
