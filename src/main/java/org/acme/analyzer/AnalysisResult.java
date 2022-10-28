package org.acme.analyzer;

import org.acme.model.Component;
import org.acme.model.Vulnerability;
import org.acme.tasks.scanners.AnalyzerIdentity;

public record AnalysisResult(Component component, Vulnerability vulnerability, AnalyzerIdentity identity) {
}
