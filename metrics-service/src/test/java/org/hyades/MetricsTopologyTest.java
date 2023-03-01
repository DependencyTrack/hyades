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

    private static final UUID COMPONENT_UUID = UUID.randomUUID();
    private static final UUID PROJECT_UUID = UUID.randomUUID();

    @Test
    void testMetrics() {
        final var component = new Component();
        UUID componentUuid = UUID.randomUUID();
        component.setUuid(componentUuid);
        component.setCpe("cpe:/a:acme:application:9.1.1");
        component.setPurl("pkg:maven/acme/a@9.1.1");
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

}
