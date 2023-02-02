package org.hyades.util;

import org.apache.commons.lang3.StringUtils;
import org.hyades.model.Severity;

public class ParsingUtil {
    public static String trimSummary(String summary) {
        int MAX_LEN = 255;
        if (summary != null && summary.length() > 255) {
            return StringUtils.substring(summary, 0, MAX_LEN - 2) + "..";
        }
        return summary;
    }
    public static Severity fromString(String severity) {
        if (severity == null) {
            return Severity.UNASSIGNED;
        }
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> Severity.CRITICAL;
            case "HIGH" -> Severity.HIGH;
            case "MODERATE", "MEDIUM" -> Severity.MEDIUM;
            case "LOW" -> Severity.LOW;
            default -> Severity.UNASSIGNED;
        };
    }
    private ParsingUtil() {
    }
}
