package org.acme.model;

import org.acme.tasks.scanners.AnalyzerIdentity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VulnerablityResult {

    Vulnerability vulnerability;
    AnalyzerIdentity identity;

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Vulnerability vulnerability) {
        this.vulnerability = vulnerability;
    }

    public AnalyzerIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(AnalyzerIdentity identity) {
        this.identity = identity;
    }
}
