package org.hyades;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.hyades.common.KafkaTopic;
import org.hyades.proto.KafkaProtobufSerde;
import org.hyades.proto.metrics.v1.ComponentMetrics;
import org.hyades.proto.metrics.v1.FindingsMetrics;
import org.hyades.proto.metrics.v1.PolicyViolationsMetrics;
import org.hyades.proto.metrics.v1.PortfolioMetrics;
import org.hyades.proto.metrics.v1.ProjectMetrics;
import org.hyades.proto.metrics.v1.VulnerabilitiesMetrics;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class MetricsTopologyTest {

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    @Test
    void testMetricsWithMultipleComponentsOfMultipleProjects() {
        UUID componentUuid1 = UUID.randomUUID();
        UUID projectUuid1 = UUID.randomUUID();
        ComponentMetrics componentMetrics1 = ComponentMetrics.newBuilder()
                .setComponentUuid(componentUuid1.toString())
                .setProjectUuid(projectUuid1.toString())
                .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                        .setTotal(9)
                        .setCritical(2)
                        .setHigh(3)
                        .setMedium(4))
                .setFindings(FindingsMetrics.newBuilder()
                        .setTotal(4)
                        .setAudited(2)
                        .setUnaudited(2))
                .setPolicyViolations(PolicyViolationsMetrics.newBuilder()
                        .setTotal(4)
                        .setAudited(2)
                        .setUnaudited(2))
                .build();

        UUID componentUuid2 = UUID.randomUUID();
        UUID projectUuid2 = UUID.randomUUID();
        ComponentMetrics componentMetrics2 = ComponentMetrics.newBuilder()
                .setComponentUuid(componentUuid2.toString())
                .setProjectUuid(projectUuid2.toString())
                .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                        .setTotal(15)
                        .setCritical(4)
                        .setHigh(5)
                        .setMedium(6))
                .build();

        //this metrics will imitate a new scan of same component with one critical vulnerability fixed
        ComponentMetrics componentMetrics3 = ComponentMetrics.newBuilder()
                .setComponentUuid(componentUuid2.toString())
                .setProjectUuid(projectUuid2.toString())
                .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                        .setTotal(14)
                        .setCritical(3)
                        .setHigh(5)
                        .setMedium(6))
                .build();

        kafkaCompanion
                .produce(new Serdes.StringSerde(), new KafkaProtobufSerde<>(ComponentMetrics.parser()))
                .fromRecords(new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid1.toString(), componentMetrics1),
                        new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid2.toString(), componentMetrics2),
                        new ProducerRecord<>(KafkaTopic.COMPONENT_METRICS.getName(), componentUuid2.toString(), componentMetrics3));

        final List<ConsumerRecord<String, ProjectMetrics>> results = kafkaCompanion
                .consume(new Serdes.StringSerde(), new KafkaProtobufSerde<>(ProjectMetrics.parser()))
                .fromTopics(KafkaTopic.PROJECT_METRICS.getName(), 2, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();

        final List<ConsumerRecord<String, PortfolioMetrics>> portfolioResults = kafkaCompanion
                .consume(new Serdes.StringSerde(), new KafkaProtobufSerde<>(PortfolioMetrics.parser()))
                .fromTopics(KafkaTopic.PORTFOLIO_METRICS.getName(), 1, Duration.ofSeconds(8))
                .awaitCompletion()
                .getRecords();

        assertThat(results).satisfiesExactlyInAnyOrder(
                record -> {
                    assertThat(record.key()).isEqualTo(projectUuid1.toString());
                    assertThat(record.value().getVulnerabilities().getCritical()).isEqualTo(2);
                    assertThat(record.value().getVulnerabilities().getHigh()).isEqualTo(3);
                    assertThat(record.value().getVulnerabilities().getMedium()).isEqualTo(4);
                    assertThat(record.value().getVulnerabilities().getLow()).isZero();
                    assertThat(record.value().getFindings().getUnaudited()).isEqualTo(2);
                    assertThat(record.value().getFindings().getAudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolations().getAudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolations().getUnaudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolations().getFail()).isZero();
                    assertThat(record.value().getPolicyViolations().getWarn()).isZero();
                    assertThat(record.value().getPolicyViolations().getInfo()).isZero();
                    assertThat(record.value().getPolicyViolations().getTotal()).isEqualTo(4);
                    assertThat(record.value().getPolicyViolations().getLicenseAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityTotal()).isZero();
                    assertThat(record.value().getComponents()).isEqualTo(1);
                },
                record -> {
                    assertThat(record.key()).isEqualTo(projectUuid2.toString());
                    assertThat(record.value().getVulnerabilities().getCritical()).isEqualTo(3);
                    assertThat(record.value().getVulnerabilities().getHigh()).isEqualTo(5);
                    assertThat(record.value().getVulnerabilities().getMedium()).isEqualTo(6);
                    assertThat(record.value().getVulnerabilities().getLow()).isZero();
                    assertThat(record.value().getComponents()).isEqualTo(1);
                    assertThat(record.value().getFindings().getUnaudited()).isZero();
                    assertThat(record.value().getFindings().getAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getFail()).isZero();
                    assertThat(record.value().getPolicyViolations().getWarn()).isZero();
                    assertThat(record.value().getPolicyViolations().getInfo()).isZero();
                    assertThat(record.value().getPolicyViolations().getTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityTotal()).isZero();
                }
        );

        assertThat(portfolioResults).satisfiesExactly(
                record -> {
                    assertThat(record.value().getProjects()).isEqualTo(2);
                    assertThat(record.value().getComponents()).isEqualTo(2);
                    assertThat(record.value().getVulnerabilities().getCritical()).isEqualTo(5);
                    assertThat(record.value().getVulnerabilities().getHigh()).isEqualTo(8);
                    assertThat(record.value().getVulnerabilities().getMedium()).isEqualTo(10);
                    assertThat(record.value().getVulnerabilities().getLow()).isZero();
                    assertThat(record.value().getFindings().getUnaudited()).isEqualTo(2);
                    assertThat(record.value().getFindings().getAudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolations().getAudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolations().getUnaudited()).isEqualTo(2);
                    assertThat(record.value().getPolicyViolations().getFail()).isZero();
                    assertThat(record.value().getPolicyViolations().getWarn()).isZero();
                    assertThat(record.value().getPolicyViolations().getInfo()).isZero();
                    assertThat(record.value().getPolicyViolations().getTotal()).isEqualTo(4);
                    assertThat(record.value().getPolicyViolations().getLicenseAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getLicenseTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getOperationalTotal()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityAudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityUnaudited()).isZero();
                    assertThat(record.value().getPolicyViolations().getSecurityTotal()).isZero();
                    assertThat(record.value().getInheritedRiskScore()).isEqualTo(120.0);
                }
        );
    }
}
