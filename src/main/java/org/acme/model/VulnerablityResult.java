package org.acme.model;

import org.acme.tasks.scanners.AnalyzerIdentity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VulnerablityResult {

    Vulnerability vulnerability;
    AnalyzerIdentity identity;
    Component component;

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


    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
