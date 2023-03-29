package org.hyades.vulnmirror.datasource.util;

import org.cyclonedx.proto.v1_4.Bom;
import org.cyclonedx.proto.v1_4.Component;
import org.cyclonedx.proto.v1_4.Severity;

import java.util.Optional;

import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_CRITICAL;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_HIGH;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_INFO;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_LOW;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_MEDIUM;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_NONE;
import static org.cyclonedx.proto.v1_4.Severity.SEVERITY_UNKNOWN;

public class ParserUtil {

    public static String getBomRefIfComponentExists(Bom cyclonedxBom, String purl) {
        if (cyclonedxBom.getComponentsList() != null && purl != null) {
            Optional<Component> existingComponent = cyclonedxBom.getComponentsList().stream().filter(c ->
                    c.getPurl().equalsIgnoreCase(purl)).findFirst();
            if (existingComponent.isPresent()) {
                return existingComponent.get().getBomRef();
            }
        }
        return null;
    }

    public static Severity mapSeverity(String severity) {
        if(severity == null) {
            return SEVERITY_UNKNOWN;
        }
        return switch (severity) {
            case "CRITICAL" -> SEVERITY_CRITICAL;
            case "HIGH" -> SEVERITY_HIGH;
            case "MEDIUM" -> SEVERITY_MEDIUM;
            case "LOW" -> SEVERITY_LOW;
            case "INFO" -> SEVERITY_INFO;
            case "NONE" -> SEVERITY_NONE;
            default -> SEVERITY_UNKNOWN;
        };
    }
}
