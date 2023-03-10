package org.hyades;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.hyades.common.KafkaTopic;
import org.hyades.metrics.model.ComponentMetrics;
import org.hyades.metrics.model.PortfolioMetrics;
import org.hyades.model.Component;
import org.hyades.model.Project;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusIntegrationTest
@QuarkusTestResource(KafkaCompanionResource.class)
class MetricsServiceIT {

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    @Test
    void test() {
        final var component1 = new Component();
        UUID componentUuid1 = UUID.randomUUID();
        component1.setUuid(componentUuid1);
        Project project1 = new Project();
        UUID projectUuid1 = UUID.randomUUID();
        project1.setUuid(projectUuid1);
        project1.setName("test1");
        component1.setProject(project1);
        ComponentMetrics componentMetrics1 = new ComponentMetrics();
        componentMetrics1.setProject(project1);
        componentMetrics1.setCritical(2);
        componentMetrics1.setHigh(3);
        componentMetrics1.setMedium(4);
        componentMetrics1.setFindingsAudited(2);
        componentMetrics1.setFindingsUnaudited(2);
        componentMetrics1.setPolicyViolationsAudited(2);
        componentMetrics1.setPolicyViolationsUnaudited(2);
        componentMetrics1.setComponent(component1);
        componentMetrics1.setProject(project1);

        final var component2 = new Component();
        UUID componentUuid2 = UUID.randomUUID();
        component2.setUuid(componentUuid2);
        Project project2 = new Project();
        project2.setName("test2");
        UUID projectUuid2 = UUID.randomUUID();
        project2.setUuid(projectUuid2);
        component2.setProject(project2);
        ComponentMetrics componentMetrics2 = new ComponentMetrics();
        componentMetrics2.setProject(project2);
        componentMetrics2.setCritical(4);
        componentMetrics2.setHigh(5);
        componentMetrics2.setMedium(6);
        componentMetrics2.setComponent(component2);


        //this metrics will imitate a new scan of same component with one critical vulnerability fixed
        ComponentMetrics componentMetrics3 = new ComponentMetrics();
        componentMetrics3.setProject(project2);
        componentMetrics3.setCritical(3);
        componentMetrics3.setHigh(5);
        componentMetrics3.setMedium(6);
        componentMetrics3.setComponent(component2);


        kafkaCompanion
                .produce(new Serdes.StringSerde(), new ObjectMapperSerde<>(ComponentMetrics.class))
                .fromRecords(new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid1.toString(), componentMetrics1),
                        new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid2.toString(), componentMetrics2),
                        new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid2.toString(), componentMetrics3));


        final List<ConsumerRecord<String, PortfolioMetrics>> portfolioResults = kafkaCompanion
                .consume(new Serdes.StringSerde(), new ObjectMapperSerde<>(PortfolioMetrics.class))
                .fromTopics(KafkaTopic.PORTFOLIO_METRICS.getName(), 1, Duration.ofSeconds(7))
                .awaitCompletion()
                .getRecords();


        assertThat(portfolioResults).satisfiesExactly(
                record -> {
                    assertThat(record.value().getProjects()).isEqualTo(2);
                    assertThat(record.value().getComponents()).isEqualTo(2);
                    assertThat(record.value().getCritical()).isEqualTo(5);
                    assertThat(record.value().getHigh()).isEqualTo(8);
                    assertThat(record.value().getMedium()).isEqualTo(10);
                    assertThat(record.value().getLow()).isZero();
                    assertThat(record.value().getFindingsUnaudited()).isEqualTo(2);
                    assertThat(record.value().getFindingsAudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolationsAudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolationsUnaudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolationsFail()).isZero();
                    assertThat(record.value().getPolicyViolationsWarn()).isZero();
                    assertThat(record.value().getPolicyViolationsInfo()).isZero();
                    assertThat(record.value().getPolicyViolationsTotal()).isZero();
                    assertThat(record.value().getPolicyViolationsLicenseAudited()).isZero();
                    assertThat(record.value().getPolicyViolationsLicenseUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolationsLicenseTotal()).isZero();
                    assertThat(record.value().getPolicyViolationsOperationalAudited()).isZero();
                    assertThat(record.value().getPolicyViolationsOperationalUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolationsOperationalTotal()).isZero();
                    assertThat(record.value().getPolicyViolationsSecurityAudited()).isZero();
                    assertThat(record.value().getPolicyViolationsSecurityUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolationsSecurityTotal()).isZero();
                    assertThat(record.value().getInheritedRiskScore()).isEqualTo(120.0);
                }
        );
    }

}
