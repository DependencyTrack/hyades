package org.hyades.metrics.model;

import org.hyades.model.Project;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectMetricsTest {

    private static final UUID PROJECT_UUID = UUID.randomUUID();

    @Test
    void shouldDecreaseVulnerableComponentCountIfDeletedComponentHadVulnerabilities() {
        ProjectMetrics projectMetrics = createProjectMetrics();
        ComponentMetrics deltaMetrics = new ComponentMetrics();
        deltaMetrics.setStatus(Status.DELETED);
        deltaMetrics.setVulnerabilities(-4);
        ProjectMetrics updated = projectMetrics.add(deltaMetrics);
        assertEquals(2, updated.getComponents());
        assertEquals(1, updated.getVulnerableComponents());
    }

    @Test
    void shouldNotDecreaseVulnerableComponentCountIfDeletedComponentHadNoVulnerabilities() {
        ProjectMetrics projectMetrics = createProjectMetrics();
        ComponentMetrics deltaMetrics = new ComponentMetrics();
        deltaMetrics.setStatus(Status.DELETED);
        deltaMetrics.setVulnerabilities(0);
        ProjectMetrics updated = projectMetrics.add(deltaMetrics);
        assertEquals(2, updated.getComponents());
        assertEquals(2, updated.getVulnerableComponents());
    }

    @Test
    void shouldNotDecreaseVulnerableComponentCountIfUpdatedComponentHasNoVulnerabilities() {
        ProjectMetrics projectMetrics = createProjectMetrics();
        ComponentMetrics deltaMetrics = new ComponentMetrics();
        deltaMetrics.setStatus(Status.UPDATED);
        deltaMetrics.setVulnerabilityStatus(VulnerabilityStatus.NOT_VULNERABLE);
        ProjectMetrics updated = projectMetrics.add(deltaMetrics);
        assertEquals(3, updated.getComponents());
        assertEquals(1, updated.getVulnerableComponents());
    }

    private static ProjectMetrics createProjectMetrics() {
        var project = new Project();
        project.setUuid(PROJECT_UUID);
        var projectMetrics = new ProjectMetrics();
        projectMetrics.setProject(project);
        projectMetrics.setComponents(3);
        projectMetrics.setVulnerableComponents(2);
        projectMetrics.setCritical(2);
        projectMetrics.setHigh(3);
        projectMetrics.setMedium(4);
        projectMetrics.setLow(5);
        projectMetrics.setVulnerabilities(14);
        projectMetrics.setVulnerableComponents(2);
        projectMetrics.setFindingsAudited(5);
        projectMetrics.setFindingsUnaudited(5);
        projectMetrics.setFindingsTotal(10);
        projectMetrics.setPolicyViolationsFail(1);
        projectMetrics.setPolicyViolationsWarn(5);
        projectMetrics.setPolicyViolationsInfo(5);
        projectMetrics.setPolicyViolationsAudited(0);
        projectMetrics.setPolicyViolationsUnaudited(0);
        projectMetrics.setFindingsUnaudited(5);
        projectMetrics.setPolicyViolationsOperationalAudited(5);
        projectMetrics.setPolicyViolationsOperationalUnaudited(5);
        projectMetrics.setPolicyViolationsOperationalTotal(10);
        projectMetrics.setPolicyViolationsSecurityAudited(5);
        projectMetrics.setPolicyViolationsSecurityUnaudited(5);
        projectMetrics.setPolicyViolationsSecurityTotal(10);
        projectMetrics.setPolicyViolationsLicenseAudited(5);
        projectMetrics.setPolicyViolationsLicenseAudited(5);
        projectMetrics.setPolicyViolationsLicenseUnaudited(5);
        projectMetrics.setPolicyViolationsLicenseTotal(10);
        return projectMetrics;
    }
}
