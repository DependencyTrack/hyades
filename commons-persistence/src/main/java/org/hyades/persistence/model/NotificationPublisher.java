package org.hyades.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@RegisterForReflection
@Table(name = "NOTIFICATIONPUBLISHER")
public class NotificationPublisher extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PUBLISHER_CLASS")
    private String publisherClass;

    @Column(name = "TEMPLATE")
    private String template;

    @Column(name = "TEMPLATE_MIME_TYPE")
    private String templateMimeType;

    @Column(name = "DEFAULT_PUBLISHER")
    private boolean defaultPublisher;

    @Column(name = "UUID")
    private UUID uuid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisherClass() {
        return publisherClass;
    }

    public void setPublisherClass(String publisherClass) {
        this.publisherClass = publisherClass;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplateMimeType() {
        return templateMimeType;
    }

    public void setTemplateMimeType(String templateMimeType) {
        this.templateMimeType = templateMimeType;
    }

    public boolean isDefaultPublisher() {
        return defaultPublisher;
    }

    public void setDefaultPublisher(boolean defaultPublisher) {
        this.defaultPublisher = defaultPublisher;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}