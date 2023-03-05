package org.hyades.metrics.util;

import org.hyades.metrics.model.ComponentMetrics;
import org.hyades.metrics.model.Metrics;
import org.hyades.metrics.model.ProjectMetrics;

public class MetricsUtil {

    private MetricsUtil() {
    }

    public static double inheritedRiskScore(final int critical, final int high, final int medium, final int low, final int unassigned) {
        return ((critical * 10) + (high * 5) + (medium * 3) + (low * 1) + (unassigned * 5));
    }

    public static boolean hasChanged(ComponentMetrics inMemoryMetrics, ComponentMetrics eventMetrics) {
        return isChanged(inMemoryMetrics, eventMetrics);
    }

    public static boolean hasChanged(ProjectMetrics inMemoryMetrics, ProjectMetrics eventMetrics) {
        return isChanged(inMemoryMetrics, eventMetrics)
                || inMemoryMetrics.getComponents() != eventMetrics.getComponents()
                || inMemoryMetrics.getVulnerableComponents() != eventMetrics.getVulnerableComponents();

    }

    private static boolean isChanged(Metrics inMemoryMetrics, Metrics eventMetrics) {
        return inMemoryMetrics.getCritical() != eventMetrics.getCritical()
                || inMemoryMetrics.getHigh() != eventMetrics.getHigh()
                || inMemoryMetrics.getMedium() != eventMetrics.getMedium()
                || inMemoryMetrics.getLow() != eventMetrics.getLow()
                || inMemoryMetrics.getUnassigned() != eventMetrics.getUnassigned()
                || inMemoryMetrics.getVulnerabilities() != eventMetrics.getVulnerabilities()
                || inMemoryMetrics.getSuppressed() != eventMetrics.getSuppressed()
                || inMemoryMetrics.getFindingsTotal() != eventMetrics.getFindingsTotal()
                || inMemoryMetrics.getFindingsAudited() != eventMetrics.getFindingsAudited()
                || inMemoryMetrics.getFindingsUnaudited() != eventMetrics.getFindingsUnaudited()
                || inMemoryMetrics.getInheritedRiskScore() != eventMetrics.getInheritedRiskScore()
                || inMemoryMetrics.getPolicyViolationsFail() != eventMetrics.getPolicyViolationsFail()
                || inMemoryMetrics.getPolicyViolationsWarn() != eventMetrics.getPolicyViolationsWarn()
                || inMemoryMetrics.getPolicyViolationsInfo() != eventMetrics.getPolicyViolationsInfo()
                || inMemoryMetrics.getPolicyViolationsTotal() != eventMetrics.getPolicyViolationsTotal()
                || inMemoryMetrics.getPolicyViolationsAudited() != eventMetrics.getPolicyViolationsAudited()
                || inMemoryMetrics.getPolicyViolationsUnaudited() != eventMetrics.getPolicyViolationsUnaudited()
                || inMemoryMetrics.getPolicyViolationsSecurityTotal() != eventMetrics.getPolicyViolationsSecurityTotal()
                || inMemoryMetrics.getPolicyViolationsSecurityAudited() != eventMetrics.getPolicyViolationsSecurityAudited()
                || inMemoryMetrics.getPolicyViolationsSecurityUnaudited() != eventMetrics.getPolicyViolationsSecurityUnaudited()
                || inMemoryMetrics.getPolicyViolationsLicenseTotal() != eventMetrics.getPolicyViolationsLicenseTotal()
                || inMemoryMetrics.getPolicyViolationsLicenseAudited() != eventMetrics.getPolicyViolationsLicenseAudited()
                || inMemoryMetrics.getPolicyViolationsLicenseUnaudited() != eventMetrics.getPolicyViolationsLicenseUnaudited()
                || inMemoryMetrics.getPolicyViolationsOperationalTotal() != eventMetrics.getPolicyViolationsOperationalTotal()
                || inMemoryMetrics.getPolicyViolationsOperationalAudited() != eventMetrics.getPolicyViolationsOperationalAudited()
                || inMemoryMetrics.getPolicyViolationsOperationalUnaudited() != eventMetrics.getPolicyViolationsOperationalUnaudited();
    }
}
