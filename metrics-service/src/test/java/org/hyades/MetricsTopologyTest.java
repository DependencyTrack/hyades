package org.hyades;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.hyades.common.KafkaTopic;
import org.hyades.metrics.model.ComponentMetrics;
import org.hyades.metrics.model.PortfolioMetrics;
import org.hyades.metrics.model.ProjectMetrics;
import org.hyades.model.Component;
import org.hyades.model.Project;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class MetricsTopologyTest {

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    private static final UUID PROJECT_UUID = UUID.randomUUID();
    private static final UUID PROJECT_UUID2 = UUID.randomUUID();

    @Test
    void testMetricsWithOneComponent() {
        final var component = new Component();
        UUID componentUuid = UUID.randomUUID();
        component.setUuid(componentUuid);
        Project project = new Project();
        project.setUuid(PROJECT_UUID);
        component.setProject(project);
        ComponentMetrics componentMetrics = new ComponentMetrics();
        componentMetrics.setProject(project);
        componentMetrics.setCritical(2);
        componentMetrics.setHigh(3);
        componentMetrics.setMedium(4);
        componentMetrics.setComponent(component);


        kafkaCompanion
                .produce(new Serdes.StringSerde(), new ObjectMapperSerde<>(ComponentMetrics.class))
                .fromRecords(new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid.toString(), componentMetrics));

        final List<ConsumerRecord<String, ProjectMetrics>> results = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(ProjectMetrics.class))
                .fromTopics(KafkaTopic.PROJECT_METRICS.getName(), 1, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();

        final List<ConsumerRecord<String, PortfolioMetrics>> portfolioResults = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(PortfolioMetrics.class))
                .fromTopics(KafkaTopic.PORTFOLIO_METRICS.getName(), 1, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();


        assertThat(results).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo(PROJECT_UUID.toString());
                    assertThat(record.value().getCritical()).isEqualTo(2);
                    assertThat(record.value().getHigh()).isEqualTo(3);
                    assertThat(record.value().getMedium()).isEqualTo(4);
                    assertThat(record.value().getComponents()).isEqualTo(1);
                }
        );

        assertThat(portfolioResults.size()).isEqualTo(1);
        assertThat(portfolioResults).satisfiesExactly(
                record -> {
                    assertThat(record.value().getProjects()).isEqualTo(1);
                    assertThat(record.value().getComponents()).isEqualTo(1);
                    assertThat(record.value().getCritical()).isEqualTo(2);
                    assertThat(record.value().getHigh()).isEqualTo(3);
                    assertThat(record.value().getMedium()).isEqualTo(4);
                    assertThat(record.value().getComponents()).isEqualTo(1);
                }
        );
    }

    @Test
    void testMetricsWithMultipleComponentsOfSingleProject() {
        final var component = new Component();
        UUID componentUuid = UUID.randomUUID();
        component.setUuid(componentUuid);
        Project project = new Project();
        project.setUuid(PROJECT_UUID);
        component.setProject(project);
        ComponentMetrics componentMetrics = new ComponentMetrics();
        componentMetrics.setProject(project);
        componentMetrics.setCritical(2);
        componentMetrics.setHigh(3);
        componentMetrics.setMedium(4);
        componentMetrics.setComponent(component);

        final var component2 = new Component();
        UUID componentUuid2 = UUID.randomUUID();
        component2.setUuid(componentUuid2);
        component2.setProject(project);
        ComponentMetrics componentMetrics2 = new ComponentMetrics();
        componentMetrics2.setProject(project);
        componentMetrics2.setCritical(3);
        componentMetrics2.setHigh(4);
        componentMetrics2.setMedium(5);
        componentMetrics2.setComponent(component2);


        kafkaCompanion
                .produce(new Serdes.StringSerde(), new ObjectMapperSerde<>(ComponentMetrics.class))
                .fromRecords(new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid.toString(), componentMetrics),
                        new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid2.toString(), componentMetrics2));

        final List<ConsumerRecord<String, ProjectMetrics>> results = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(ProjectMetrics.class))
                .fromTopics(KafkaTopic.PROJECT_METRICS.getName(), 1, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();

        final List<ConsumerRecord<String, PortfolioMetrics>> portfolioResults = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(PortfolioMetrics.class))
                .fromTopics(KafkaTopic.PORTFOLIO_METRICS.getName(), 1, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();


        assertThat(results).satisfiesExactly(
                record -> {
                    assertThat(record.key()).isEqualTo(PROJECT_UUID.toString());
                    assertThat(record.value().getCritical()).isEqualTo(5);
                    assertThat(record.value().getHigh()).isEqualTo(7);
                    assertThat(record.value().getMedium()).isEqualTo(9);
                    assertThat(record.value().getComponents()).isEqualTo(2);
                }
        );

        assertThat(portfolioResults.size()).isEqualTo(1);
        assertThat(portfolioResults).satisfiesExactly(
                record -> {
                    assertThat(record.value().getProjects()).isEqualTo(1);
                    assertThat(record.value().getComponents()).isEqualTo(2);
                    assertThat(record.value().getCritical()).isEqualTo(5);
                    assertThat(record.value().getHigh()).isEqualTo(7);
                    assertThat(record.value().getMedium()).isEqualTo(9);
                }
        );
    }

    @Test
    void testMetricsWithMultipleComponentsOfMultipleProjects() {
        final var component1 = new Component();
        UUID componentUuid1 = UUID.randomUUID();
        component1.setUuid(componentUuid1);
        Project project1 = new Project();
        project1.setUuid(PROJECT_UUID);
        project1.setName("test1");
        component1.setProject(project1);
        ComponentMetrics componentMetrics1 = new ComponentMetrics();
        componentMetrics1.setProject(project1);
        componentMetrics1.setCritical(2);
        componentMetrics1.setHigh(3);
        componentMetrics1.setMedium(4);
        componentMetrics1.setComponent(component1);

        final var component2 = new Component();
        UUID componentUuid2 = UUID.randomUUID();
        component2.setUuid(componentUuid2);
        Project project2 = new Project();
        project1.setName("test2");
        project2.setUuid(PROJECT_UUID2);
        component1.setProject(project1);
        ComponentMetrics componentMetrics2 = new ComponentMetrics();
        componentMetrics2.setProject(project2);
        componentMetrics2.setCritical(4);
        componentMetrics2.setHigh(5);
        componentMetrics2.setMedium(6);
        componentMetrics2.setComponent(component2);


        kafkaCompanion
                .produce(new Serdes.StringSerde(), new ObjectMapperSerde<>(ComponentMetrics.class))
                .fromRecords(new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid1.toString(), componentMetrics1),
                        new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid2.toString(), componentMetrics2));

        final List<ConsumerRecord<String, ProjectMetrics>> results = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(ProjectMetrics.class))
                .fromTopics(KafkaTopic.PROJECT_METRICS.getName(), 2, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();

        final List<ConsumerRecord<String, PortfolioMetrics>> portfolioResults = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(PortfolioMetrics.class))
                .fromTopics(KafkaTopic.PORTFOLIO_METRICS.getName(), 1, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();


        assertThat(results).satisfiesExactlyInAnyOrder(
                record -> {
                    assertThat(record.key()).isEqualTo(PROJECT_UUID.toString());
                    assertThat(record.value().getCritical()).isEqualTo(2);
                    assertThat(record.value().getHigh()).isEqualTo(3);
                    assertThat(record.value().getMedium()).isEqualTo(4);
                    assertThat(record.value().getComponents()).isEqualTo(1);
                },
                record -> {
                    assertThat(record.key()).isEqualTo(PROJECT_UUID2.toString());
                    assertThat(record.value().getCritical()).isEqualTo(4);
                    assertThat(record.value().getHigh()).isEqualTo(5);
                    assertThat(record.value().getMedium()).isEqualTo(6);
                    assertThat(record.value().getComponents()).isEqualTo(1);
                }
        );

        assertThat(portfolioResults.size()).isEqualTo(1);
        assertThat(portfolioResults).satisfiesExactly(
                record -> {
                    assertThat(record.value().getProjects()).isEqualTo(2);
                    assertThat(record.value().getComponents()).isEqualTo(2);
                    assertThat(record.value().getCritical()).isEqualTo(6);
                    assertThat(record.value().getHigh()).isEqualTo(8);
                    assertThat(record.value().getMedium()).isEqualTo(10);
                }
        );
    }
}
