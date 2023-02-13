package org.hyades.util;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;

import java.util.Optional;

public class ParserUtil {

    public static String getBomRefIfComponentExists(Bom cyclonedxBom, String purl) {
        if (cyclonedxBom.getComponents() != null && purl != null) {
            Optional<Component> existingComponent = cyclonedxBom.getComponents().stream().filter(c ->
                    c.getPurl().equalsIgnoreCase(purl)).findFirst();
            if (existingComponent.isPresent()) {
                return existingComponent.get().getBomRef();
            }
        }
        return null;
    }
}
