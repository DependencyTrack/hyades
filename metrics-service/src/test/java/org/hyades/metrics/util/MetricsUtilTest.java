package org.hyades.metrics.util;

import org.hyades.proto.metrics.v1.ComponentMetrics;
import org.hyades.proto.metrics.v1.FindingsMetrics;
import org.hyades.proto.metrics.v1.PolicyViolationsMetrics;
import org.hyades.proto.metrics.v1.PortfolioMetrics;
import org.hyades.proto.metrics.v1.ProjectMetrics;
import org.hyades.proto.metrics.v1.VulnerabilitiesMetrics;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hyades.proto.metrics.v1.Status.STATUS_DELETED;
import static org.hyades.proto.metrics.v1.Status.STATUS_UPDATED;
import static org.hyades.proto.metrics.v1.VulnerabilityStatus.VULNERABILITY_STATUS_NOT_VULNERABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsUtilTest {

    @Nested
    class ProjectMetricsTest {

        private static final UUID PROJECT_UUID = UUID.randomUUID();

        @Test
        void shouldDecreaseVulnerableComponentCountIfDeletedComponentHadVulnerabilities() {
            ProjectMetrics projectMetrics = createProjectMetrics();
            ComponentMetrics deltaMetrics = ComponentMetrics.newBuilder()
                    .setStatus(STATUS_DELETED)
                    .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                            .setTotal(-4))
                    .build();
            ProjectMetrics updated = MetricsUtil.add(projectMetrics, deltaMetrics);
            assertEquals(2, updated.getComponents());
            assertEquals(1, updated.getVulnerableComponents());
        }

        @Test
        void shouldNotDecreaseVulnerableComponentCountIfDeletedComponentHadNoVulnerabilities() {
            ProjectMetrics projectMetrics = createProjectMetrics();
            ComponentMetrics deltaMetrics = ComponentMetrics.newBuilder()
                    .setStatus(STATUS_DELETED)
                    .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                            .setTotal(0))
                    .build();
            ProjectMetrics updated = MetricsUtil.add(projectMetrics, deltaMetrics);
            assertEquals(2, updated.getComponents());
            assertEquals(2, updated.getVulnerableComponents());
        }

        @Test
        void shouldNotDecreaseVulnerableComponentCountIfUpdatedComponentHasNoVulnerabilities() {
            ProjectMetrics projectMetrics = createProjectMetrics();
            ComponentMetrics deltaMetrics = ComponentMetrics.newBuilder()
                    .setStatus(STATUS_UPDATED)
                    .setVulnerabilityStatus(VULNERABILITY_STATUS_NOT_VULNERABLE)
                    .build();
            ProjectMetrics updated = MetricsUtil.add(projectMetrics, deltaMetrics);
            assertEquals(3, updated.getComponents());
            assertEquals(1, updated.getVulnerableComponents());
        }

        private static ProjectMetrics createProjectMetrics() {
            return ProjectMetrics.newBuilder()
                    .setProjectUuid(PROJECT_UUID.toString())
                    .setComponents(3)
                    .setVulnerableComponents(2)
                    .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                            .setTotal(14)
                            .setCritical(2)
                            .setHigh(3)
                            .setMedium(4)
                            .setLow(5))
                    .setFindings(FindingsMetrics.newBuilder()
                            .setTotal(10)
                            .setAudited(5)
                            .setUnaudited(5))
                    .setPolicyViolations(PolicyViolationsMetrics.newBuilder()
                            .setTotal(30)
                            .setFail(1)
                            .setWarn(5)
                            .setInfo(5)
                            .setLicenseTotal(10)
                            .setLicenseAudited(5)
                            .setLicenseUnaudited(5)
                            .setOperationalTotal(10)
                            .setOperationalAudited(5)
                            .setOperationalUnaudited(5)
                            .setSecurityTotal(10)
                            .setSecurityAudited(5)
                            .setSecurityUnaudited(5))
                    .build();
        }

    }

    @Nested
    class PortfolioMetricsTest {

        @Test
        void shouldDecreaseVulnerableComponentCountIfDeletedComponentHadVulnerabilities() {
            PortfolioMetrics portfolioMetrics = createPortfolioMetrics();
            ProjectMetrics deltaMetrics = ProjectMetrics.newBuilder()
                    .setStatus(STATUS_DELETED)
                    .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                            .setTotal(-4))
                    .build();
            PortfolioMetrics updated = MetricsUtil.add(portfolioMetrics, deltaMetrics);
            assertEquals(2, updated.getProjects());
            assertEquals(1, updated.getVulnerableProjects());
        }

        @Test
        void shouldNotDecreaseVulnerableComponentCountIfDeletedComponentHadNoVulnerabilities() {
            PortfolioMetrics portfolioMetrics = createPortfolioMetrics();
            ProjectMetrics deltaMetrics = ProjectMetrics.newBuilder()
                    .setStatus(STATUS_DELETED)
                    .setVulnerabilities(VulnerabilitiesMetrics.newBuilder()
                            .setTotal(0))
                    .build();
            PortfolioMetrics updated = MetricsUtil.add(portfolioMetrics, deltaMetrics);
            assertEquals(2, updated.getProjects());
            assertEquals(2, updated.getVulnerableProjects());
        }

        @Test
        void shouldNotDecreaseVulnerableComponentCountIfUpdatedComponentHasNoVulnerabilities() {
            PortfolioMetrics portfolioMetrics = createPortfolioMetrics();
            ProjectMetrics deltaMetrics = ProjectMetrics.newBuilder()
                    .setStatus(STATUS_UPDATED)
                    .setVulnerabilityStatus(VULNERABILITY_STATUS_NOT_VULNERABLE)
                    .build();
            PortfolioMetrics updated = MetricsUtil.add(portfolioMetrics, deltaMetrics);
            assertEquals(3, updated.getProjects());
            assertEquals(1, updated.getVulnerableProjects());
        }

        private PortfolioMetrics createPortfolioMetrics() {
            return PortfolioMetrics.newBuilder()
                    .setProjects(3)
                    .setVulnerableProjects(2)
                    .build();
        }

    }

}