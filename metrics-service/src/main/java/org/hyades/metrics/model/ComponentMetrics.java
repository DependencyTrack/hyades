package org.hyades.metrics.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.model.Component;
import org.hyades.model.Project;

import java.io.Serializable;

import static org.hyades.metrics.model.Status.UNKNOWN;

@RegisterForReflection
public class ComponentMetrics extends Counters implements Serializable {

    private Project project;

    private Component component;

    private Status status = UNKNOWN;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
