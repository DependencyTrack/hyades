package org.hyades.metrics.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PortfolioMetricsTest {

    @Test
    void shouldDecreaseVulnerableComponentCountIfDeletedComponentHadVulnerabilities() {
        PortfolioMetrics projectMetrics = createPortfolioMetrics();
        ProjectMetrics deltaMetrics = new ProjectMetrics();
        deltaMetrics.setStatus(Status.DELETED);
        deltaMetrics.setVulnerabilities(-4);
        PortfolioMetrics updated = projectMetrics.add(deltaMetrics);
        assertEquals(2, updated.getProjects());
        assertEquals(1, updated.getVulnerableProjects());
    }

    @Test
    void shouldNotDecreaseVulnerableComponentCountIfDeletedComponentHadNoVulnerabilities() {
        PortfolioMetrics projectMetrics = createPortfolioMetrics();
        ProjectMetrics deltaMetrics = new ProjectMetrics();
        deltaMetrics.setStatus(Status.DELETED);
        deltaMetrics.setVulnerabilities(0);
        PortfolioMetrics updated = projectMetrics.add(deltaMetrics);
        assertEquals(2, updated.getProjects());
        assertEquals(2, updated.getVulnerableProjects());
    }

    @Test
    void shouldNotDecreaseVulnerableComponentCountIfUpdatedComponentHasNoVulnerabilities() {
        PortfolioMetrics portfolioMetrics = createPortfolioMetrics();
        ProjectMetrics deltaMetrics = new ProjectMetrics();
        deltaMetrics.setStatus(Status.UPDATED);
        deltaMetrics.setVulnerabilityStatus(VulnerabilityStatus.NOT_VULNERABLE);
        PortfolioMetrics updated = portfolioMetrics.add(deltaMetrics);
        assertEquals(3, updated.getProjects());
        assertEquals(1, updated.getVulnerableProjects());
    }

    private PortfolioMetrics createPortfolioMetrics() {
        PortfolioMetrics portfolioMetrics = new PortfolioMetrics();
        portfolioMetrics.setProjects(3);
        portfolioMetrics.setVulnerableProjects(2);
        return portfolioMetrics;
    }
}
