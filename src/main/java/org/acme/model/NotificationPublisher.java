package org.acme.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.acme.common.TrimmedStringDeserializer;
import org.acme.persistence.LongToIntConverter;
import org.acme.persistence.UUIDConverter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @GeneratedValue(strategy=GenerationType.AUTO)
    @JsonIgnore
    @Column(name="ID")
    @Convert(converter = LongToIntConverter.class)
    private int id;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
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