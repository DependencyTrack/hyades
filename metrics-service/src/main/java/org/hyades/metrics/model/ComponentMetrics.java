package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.model.Component;
import org.hyades.model.Project;

import java.io.Serializable;

@RegisterForReflection
public class ComponentMetrics extends Metrics implements Serializable {

    private Project project;

    private Component component;


    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
