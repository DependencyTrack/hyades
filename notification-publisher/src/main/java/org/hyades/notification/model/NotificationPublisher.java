package org.hyades.notification.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.hyades.common.TrimmedStringDeserializer;
import org.hyades.persistence.UUIDConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;
/**
 * Defines a Model class for notification publisher definitions.
 *
 * @author Steve Springett
 * @since 3.2.0
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
@Table(name = "NOTIFICATIONPUBLISHER", uniqueConstraints = {@UniqueConstraint(columnNames = {"UUID"}, name = "NOTIFICATIONPUBLISHER_UUID_IDX")})
public class NotificationPublisher extends PanacheEntityBase implements Serializable {

    private static final long serialVersionUID = -1274494967231181534L;
    /**
     * Defines JDO fetch groups for this class.
     */
    public enum FetchGroup {
        ALL
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name="ID")
    private long id;

    //@Persistent(defaultFetchGroup = "true")
    //@Column(lenght = "NAME", allowsNull = "false")
    @Column(name = "NAME", nullable = false )
    @NotBlank
    @Size(min = 1, max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String name;

    @Column(name = "DESCRIPTION")
    @Size(min = 0, max = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String description;

    @Column(name = "PUBLISHER_CLASS", length = 1024,nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String publisherClass;

    @Column(name = "TEMPLATE")
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String template;

    @Column(name = "TEMPLATE_MIME_TYPE", nullable = false)
    @NotBlank
    @Size(min = 1, max = 255)
    @JsonDeserialize(using = TrimmedStringDeserializer.class)
    private String templateMimeType;

    @Column(name = "DEFAULT_PUBLISHER")
    private boolean defaultPublisher;

    @Column(name = "UUID", columnDefinition = "varchar", length = 36, nullable = false, unique = true)
    @NotNull
    @Convert(converter = UUIDConverter.class)
    private UUID uuid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    public String getPublisherClass() {
        return publisherClass;
    }

    public void setPublisherClass(@NotNull String publisherClass) {
        this.publisherClass = publisherClass;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @NotNull
    public String getTemplateMimeType() {
        return templateMimeType;
    }

    public void setTemplateMimeType(@NotNull String templateMimeType) {
        this.templateMimeType = templateMimeType;
    }

    public boolean isDefaultPublisher() {
        return defaultPublisher;
    }

    public void setDefaultPublisher(boolean defaultPublisher) {
        this.defaultPublisher = defaultPublisher;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@NotNull UUID uuid) {
        this.uuid = uuid;
    }
}