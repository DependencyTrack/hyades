package org.acme.analyzer;

import org.acme.model.Component;
import org.acme.model.VulnerabilityResult;

import java.util.List;

public interface Analyzer {

    boolean isEnabled();

    List<VulnerabilityResult> analyze(final List<Component> components);

}
