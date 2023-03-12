package org.hyades.metrics.util;

import org.hyades.proto.metrics.v1.ComponentMetrics;
import org.hyades.proto.metrics.v1.FindingsMetrics;
import org.hyades.proto.metrics.v1.PolicyViolationsMetrics;
import org.hyades.proto.metrics.v1.PortfolioMetrics;
import org.hyades.proto.metrics.v1.ProjectMetrics;
import org.hyades.proto.metrics.v1.VulnerabilitiesMetrics;

import static org.hyades.proto.metrics.v1.Status.STATUS_DELETED;
import static org.hyades.proto.metrics.v1.Status.STATUS_UNCHANGED;
import static org.hyades.proto.metrics.v1.Status.STATUS_UPDATED;

public class MetricsUtil {

    private MetricsUtil() {
    }

    public static double inheritedRiskScore(final int critical, final int high, final int medium, final int low, final int unassigned) {
        return ((critical * 10) + (high * 5) + (medium * 3) + (low * 1) + (unassigned * 5));
    }

    public static double inheritedRiskScore(final VulnerabilitiesMetrics metrics) {
        return inheritedRiskScore(metrics.getCritical(), metrics.getHigh(), metrics.getMedium(), metrics.getLow(), metrics.getUnassigned());
    }

    public static boolean hasChanged(ComponentMetrics inMemoryMetrics, ComponentMetrics eventMetrics) {
        return isChanged(inMemoryMetrics.getVulnerabilities(), eventMetrics.getVulnerabilities())
                || isChanged(inMemoryMetrics.getFindings(), eventMetrics.getFindings())
                || isChanged(inMemoryMetrics.getPolicyViolations(), eventMetrics.getPolicyViolations());
    }

    public static boolean hasChanged(ProjectMetrics inMemoryMetrics, ProjectMetrics eventMetrics) {
        return isChanged(inMemoryMetrics.getVulnerabilities(), eventMetrics.getVulnerabilities())
                || isChanged(inMemoryMetrics.getFindings(), eventMetrics.getFindings())
                || isChanged(inMemoryMetrics.getPolicyViolations(), eventMetrics.getPolicyViolations())
                || inMemoryMetrics.getComponents() != eventMetrics.getComponents()
                || inMemoryMetrics.getVulnerableComponents() != eventMetrics.getVulnerableComponents();
    }

    private static boolean isChanged(final VulnerabilitiesMetrics inMemoryMetrics,
                                     final VulnerabilitiesMetrics eventMetrics) {
        return inMemoryMetrics.getTotal() != eventMetrics.getTotal()
                || inMemoryMetrics.getCritical() != eventMetrics.getCritical()
                || inMemoryMetrics.getHigh() != eventMetrics.getHigh()
                || inMemoryMetrics.getMedium() != eventMetrics.getMedium()
                || inMemoryMetrics.getLow() != eventMetrics.getLow()
                || inMemoryMetrics.getUnassigned() != eventMetrics.getUnassigned();
    }

    private static boolean isChanged(final FindingsMetrics inMemoryMetrics,
                                     final FindingsMetrics eventMetrics) {
        return inMemoryMetrics.getTotal() != eventMetrics.getTotal()
                || inMemoryMetrics.getAudited() != inMemoryMetrics.getAudited()
                || inMemoryMetrics.getUnaudited() != inMemoryMetrics.getUnaudited()
                || inMemoryMetrics.getSuppressed() != inMemoryMetrics.getSuppressed();
    }

    private static boolean isChanged(final PolicyViolationsMetrics inMemoryMetrics,
                                     final PolicyViolationsMetrics eventMetrics) {
        return inMemoryMetrics.getTotal() != eventMetrics.getTotal()
                || inMemoryMetrics.getFail() != eventMetrics.getFail()
                || inMemoryMetrics.getWarn() != eventMetrics.getWarn()
                || inMemoryMetrics.getInfo() != eventMetrics.getInfo()
                || inMemoryMetrics.getLicenseTotal() != eventMetrics.getLicenseTotal()
                || inMemoryMetrics.getLicenseAudited() != eventMetrics.getLicenseAudited()
                || inMemoryMetrics.getLicenseUnaudited() != eventMetrics.getLicenseUnaudited()
                || inMemoryMetrics.getOperationalTotal() != eventMetrics.getOperationalTotal()
                || inMemoryMetrics.getOperationalAudited() != eventMetrics.getOperationalAudited()
                || inMemoryMetrics.getOperationalUnaudited() != eventMetrics.getOperationalUnaudited()
                || inMemoryMetrics.getSecurityTotal() != eventMetrics.getSecurityTotal()
                || inMemoryMetrics.getSecurityAudited() != eventMetrics.getSecurityAudited()
                || inMemoryMetrics.getSecurityUnaudited() != eventMetrics.getSecurityUnaudited();
    }

    public static ComponentMetrics calculateDelta(final ComponentMetrics inMemoryMetrics,
                                                  final ComponentMetrics eventMetrics) {
        return ComponentMetrics.newBuilder()
                .setComponentUuid(eventMetrics.getComponentUuid())
                .setProjectUuid(eventMetrics.getProjectUuid())
                .setStatus(hasChanged(inMemoryMetrics, eventMetrics)
                        ? STATUS_UPDATED
                        : STATUS_UNCHANGED)
                .setVulnerabilities(calculateDelta(inMemoryMetrics.getVulnerabilities(), eventMetrics.getVulnerabilities()))
                .setFindings(calculateDelta(inMemoryMetrics.getFindings(), eventMetrics.getFindings()))
                .setPolicyViolations(calculateDelta(inMemoryMetrics.getPolicyViolations(), eventMetrics.getPolicyViolations()))
                .build();
    }

    public static ProjectMetrics calculateDelta(final ProjectMetrics inMemoryMetrics,
                                                final ProjectMetrics eventMetrics) {
        return ProjectMetrics.newBuilder()
                .setProjectUuid(eventMetrics.getProjectUuid())
                .setStatus(hasChanged(inMemoryMetrics, eventMetrics)
                        ? STATUS_UPDATED
                        : STATUS_UNCHANGED)
                .setComponents(eventMetrics.getComponents() - inMemoryMetrics.getComponents())
                .setVulnerableComponents(eventMetrics.getVulnerableComponents() - inMemoryMetrics.getVulnerableComponents())
                .setVulnerabilities(calculateDelta(inMemoryMetrics.getVulnerabilities(), eventMetrics.getVulnerabilities()))
                .setFindings(calculateDelta(inMemoryMetrics.getFindings(), eventMetrics.getFindings()))
                .setPolicyViolations(calculateDelta(inMemoryMetrics.getPolicyViolations(), eventMetrics.getPolicyViolations()))
                .build();
    }

    private static VulnerabilitiesMetrics calculateDelta(final VulnerabilitiesMetrics inMemoryMetrics,
                                                         final VulnerabilitiesMetrics eventMetrics) {
        return VulnerabilitiesMetrics.newBuilder()
                .setTotal(eventMetrics.getTotal() - inMemoryMetrics.getTotal())
                .setCritical(eventMetrics.getCritical() - inMemoryMetrics.getCritical())
                .setHigh(eventMetrics.getHigh() - inMemoryMetrics.getHigh())
                .setMedium(eventMetrics.getMedium() - inMemoryMetrics.getMedium())
                .setLow(eventMetrics.getLow() - inMemoryMetrics.getLow())
                .setUnassigned(eventMetrics.getUnassigned() - inMemoryMetrics.getUnassigned())
                .build();
    }

    private static FindingsMetrics calculateDelta(final FindingsMetrics inMemoryMetrics,
                                                  final FindingsMetrics eventMetrics) {
        return FindingsMetrics.newBuilder()
                .setTotal(eventMetrics.getTotal() - inMemoryMetrics.getTotal())
                .setAudited(eventMetrics.getAudited() - inMemoryMetrics.getAudited())
                .setUnaudited(eventMetrics.getUnaudited() - inMemoryMetrics.getUnaudited())
                .setSuppressed(eventMetrics.getSuppressed() - inMemoryMetrics.getSuppressed())
                .build();
    }

    private static PolicyViolationsMetrics calculateDelta(final PolicyViolationsMetrics inMemoryMetrics,
                                                          final PolicyViolationsMetrics eventMetrics) {
        return PolicyViolationsMetrics.newBuilder()
                .setTotal(eventMetrics.getTotal() - inMemoryMetrics.getTotal())
                .setFail(eventMetrics.getFail() - inMemoryMetrics.getFail())
                .setWarn(eventMetrics.getWarn() - inMemoryMetrics.getWarn())
                .setInfo(eventMetrics.getInfo() - inMemoryMetrics.getInfo())
                .setAudited(eventMetrics.getAudited() - inMemoryMetrics.getAudited())
                .setUnaudited(eventMetrics.getUnaudited() - inMemoryMetrics.getUnaudited())
                .setLicenseTotal(eventMetrics.getLicenseTotal() - inMemoryMetrics.getLicenseTotal())
                .setLicenseAudited(eventMetrics.getLicenseAudited() - inMemoryMetrics.getLicenseAudited())
                .setLicenseUnaudited(eventMetrics.getLicenseUnaudited() - inMemoryMetrics.getLicenseUnaudited())
                .setOperationalTotal(eventMetrics.getOperationalTotal() - inMemoryMetrics.getOperationalTotal())
                .setOperationalAudited(eventMetrics.getOperationalAudited() - inMemoryMetrics.getOperationalAudited())
                .setOperationalUnaudited(eventMetrics.getOperationalUnaudited() - inMemoryMetrics.getOperationalUnaudited())
                .setSecurityTotal(eventMetrics.getSecurityTotal() - inMemoryMetrics.getSecurityTotal())
                .setSecurityAudited(eventMetrics.getSecurityAudited() - inMemoryMetrics.getSecurityAudited())
                .setSecurityUnaudited(eventMetrics.getSecurityUnaudited() - inMemoryMetrics.getSecurityUnaudited())
                .build();
    }

    public static ComponentMetrics deleted(final ComponentMetrics metrics) {
        return ComponentMetrics.newBuilder()
                .setComponentUuid(metrics.getComponentUuid())
                .setProjectUuid(metrics.getProjectUuid())
                .setStatus(STATUS_DELETED)
                .setVulnerabilities(deleted(metrics.getVulnerabilities()))
                .setFindings(deleted(metrics.getFindings()))
                .setPolicyViolations(deleted(metrics.getPolicyViolations()))
                .build();
    }

    public static ProjectMetrics deleted(final ProjectMetrics metrics) {
        return ProjectMetrics.newBuilder()
                .setProjectUuid(metrics.getProjectUuid())
                .setStatus(STATUS_DELETED)
                .setComponents(-metrics.getComponents())
                .setVulnerableComponents(-metrics.getVulnerableComponents())
                .setVulnerabilities(deleted(metrics.getVulnerabilities()))
                .setFindings(deleted(metrics.getFindings()))
                .setPolicyViolations(deleted(metrics.getPolicyViolations()))
                .build();
    }

    private static VulnerabilitiesMetrics deleted(final VulnerabilitiesMetrics metrics) {
        return VulnerabilitiesMetrics.newBuilder()
                .setTotal(-metrics.getTotal())
                .setCritical(-metrics.getCritical())
                .setHigh(-metrics.getHigh())
                .setMedium(-metrics.getMedium())
                .setLow(-metrics.getLow())
                .setUnassigned(-metrics.getUnassigned())
                .build();
    }

    private static FindingsMetrics deleted(final FindingsMetrics metrics) {
        return FindingsMetrics.newBuilder()
                .setTotal(-metrics.getTotal())
                .setAudited(-metrics.getAudited())
                .setUnaudited(-metrics.getUnaudited())
                .setSuppressed(-metrics.getSuppressed())
                .build();
    }

    private static PolicyViolationsMetrics deleted(final PolicyViolationsMetrics metrics) {
        return PolicyViolationsMetrics.newBuilder()
                .setTotal(-metrics.getTotal())
                .setFail(-metrics.getFail())
                .setWarn(-metrics.getWarn())
                .setInfo(-metrics.getInfo())
                .setAudited(-metrics.getAudited())
                .setUnaudited(-metrics.getUnaudited())
                .setLicenseTotal(-metrics.getLicenseTotal())
                .setLicenseAudited(-metrics.getLicenseAudited())
                .setLicenseUnaudited(-metrics.getLicenseUnaudited())
                .setOperationalTotal(-metrics.getOperationalTotal())
                .setOperationalAudited(-metrics.getOperationalAudited())
                .setOperationalUnaudited(-metrics.getOperationalUnaudited())
                .setSecurityTotal(-metrics.getSecurityTotal())
                .setSecurityAudited(-metrics.getSecurityAudited())
                .setSecurityUnaudited(-metrics.getSecurityUnaudited())
                .build();
    }

    public static ProjectMetrics add(final ProjectMetrics projectMetrics,
                                     final ComponentMetrics componentMetrics) {
        final ProjectMetrics.Builder resultBuilder = ProjectMetrics.newBuilder(projectMetrics);

        resultBuilder.setStatus(switch (componentMetrics.getStatus()) {
            case STATUS_CREATED, STATUS_UPDATED, STATUS_DELETED -> STATUS_UPDATED;
            default -> STATUS_UNCHANGED;
        });

        switch (componentMetrics.getStatus()) {
            case STATUS_CREATED -> resultBuilder
                    .setProjectUuid(componentMetrics.getProjectUuid())
                    .setComponents(projectMetrics.getComponents() + 1);
            case STATUS_DELETED -> resultBuilder
                    .setComponents(projectMetrics.getComponents() - 1);
        }

        if (componentMetrics.getVulnerabilities().getTotal() > 0) {
            resultBuilder.setVulnerableComponents(projectMetrics.getVulnerableComponents() + 1);
        } else if (componentMetrics.getVulnerabilities().getTotal() < 0) {
            resultBuilder.setVulnerableComponents(projectMetrics.getVulnerableComponents() - 1);
        }

        resultBuilder
                .setVulnerabilities(add(projectMetrics.getVulnerabilities(), componentMetrics.getVulnerabilities()))
                .setFindings(add(projectMetrics.getFindings(), componentMetrics.getFindings()))
                .setPolicyViolations(add(projectMetrics.getPolicyViolations(), componentMetrics.getPolicyViolations()))
                .setInheritedRiskScore(inheritedRiskScore(resultBuilder.getVulnerabilities()));

        return resultBuilder.build();
    }

    public static PortfolioMetrics add(final PortfolioMetrics portfolioMetrics,
                                       final ProjectMetrics projectMetrics) {
        final PortfolioMetrics.Builder resultBuilder = PortfolioMetrics.newBuilder(portfolioMetrics);

        resultBuilder.setStatus(switch (projectMetrics.getStatus()) {
            case STATUS_CREATED, STATUS_UPDATED, STATUS_DELETED -> STATUS_UPDATED;
            default -> STATUS_UNCHANGED;
        });

        resultBuilder.setProjects(switch (projectMetrics.getStatus()) {
            case STATUS_CREATED -> portfolioMetrics.getProjects() + 1;
            case STATUS_DELETED -> portfolioMetrics.getProjects() - 1;
            default -> portfolioMetrics.getProjects();
        });

        if (projectMetrics.getVulnerabilities().getTotal() > 0) {
            resultBuilder.setVulnerableProjects(portfolioMetrics.getVulnerableProjects() + 1);
        } else if (projectMetrics.getVulnerabilities().getTotal() < 0) {
            resultBuilder.setVulnerableProjects(portfolioMetrics.getVulnerableProjects() - 1);
        }

        resultBuilder
                .setComponents(portfolioMetrics.getComponents() + projectMetrics.getComponents())
                .setVulnerableComponents(portfolioMetrics.getVulnerableComponents() + projectMetrics.getVulnerableComponents())
                .setVulnerabilities(add(portfolioMetrics.getVulnerabilities(), projectMetrics.getVulnerabilities()))
                .setFindings(add(portfolioMetrics.getFindings(), projectMetrics.getFindings()))
                .setPolicyViolations(add(portfolioMetrics.getPolicyViolations(), projectMetrics.getPolicyViolations()))
                .setInheritedRiskScore(inheritedRiskScore(resultBuilder.getVulnerabilities()));

        return resultBuilder.build();
    }

    private static VulnerabilitiesMetrics add(final VulnerabilitiesMetrics left, final VulnerabilitiesMetrics right) {
        return VulnerabilitiesMetrics.newBuilder()
                .setTotal(left.getTotal() + right.getTotal())
                .setCritical(left.getCritical() + right.getCritical())
                .setHigh(left.getHigh() + right.getHigh())
                .setMedium(left.getMedium() + right.getMedium())
                .setLow(left.getLow() + right.getLow())
                .setUnassigned(left.getUnassigned() + right.getUnassigned())
                .build();
    }

    private static FindingsMetrics add(final FindingsMetrics left, final FindingsMetrics right) {
        return FindingsMetrics.newBuilder()
                .setTotal(left.getTotal() + right.getTotal())
                .setAudited(left.getAudited() + right.getAudited())
                .setUnaudited(left.getUnaudited() + right.getUnaudited())
                .setSuppressed(left.getSuppressed() + right.getSuppressed())
                .build();
    }

    private static PolicyViolationsMetrics add(final PolicyViolationsMetrics left,
                                               final PolicyViolationsMetrics right) {
        return PolicyViolationsMetrics.newBuilder()
                .setTotal(left.getTotal() + right.getTotal())
                .setFail(left.getFail() + right.getFail())
                .setWarn(left.getWarn() + right.getWarn())
                .setInfo(left.getInfo() + right.getInfo())
                .setAudited(left.getAudited() + right.getAudited())
                .setUnaudited(left.getUnaudited() + right.getUnaudited())
                .setLicenseTotal(left.getLicenseTotal() + right.getLicenseTotal())
                .setLicenseAudited(left.getLicenseAudited() + right.getLicenseAudited())
                .setLicenseUnaudited(left.getLicenseUnaudited() + right.getLicenseUnaudited())
                .setOperationalTotal(left.getOperationalTotal() + right.getOperationalTotal())
                .setOperationalAudited(left.getOperationalAudited() + right.getOperationalAudited())
                .setOperationalUnaudited(left.getOperationalUnaudited() + right.getOperationalUnaudited())
                .setSecurityTotal(left.getSecurityTotal() + right.getSecurityTotal())
                .setSecurityAudited(left.getSecurityAudited() + right.getSecurityAudited())
                .setSecurityUnaudited(left.getSecurityUnaudited() + right.getSecurityUnaudited())
                .build();
    }

}
