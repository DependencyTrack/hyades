package org.hyades.metrics.util;

public class MetricsUtil {
    public static double inheritedRiskScore(final int critical, final int high, final int medium, final int low, final int unassigned) {
        return (double) ((critical * 10) + (high * 5) + (medium * 3) + (low * 1) + (unassigned * 5));
    }
}
