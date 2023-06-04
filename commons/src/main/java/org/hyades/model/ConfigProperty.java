//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.hyades.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.hyades.common.IConfigProperty;
import org.hyades.persistence.PropertyTypeConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "CONFIGPROPERTY")
@JsonInclude(Include.NON_NULL)
public class ConfigProperty implements IConfigProperty {
    private static final long serialVersionUID = 5286421336166302912L;
    @Id
    @JsonIgnore
    @Column(name = "ID")
    private long id;

    @Column(
            name = "GROUPNAME",
            nullable = false)

    private @NotBlank @Size(
            min = 1,
            max = 255
    ) @Pattern(
            regexp = "[\\P{Cc}]+",
            message = "The groupName must not contain control characters"
    ) String groupName;

    @Column(
            name = "PROPERTYNAME",
            nullable = false   )
    private @NotBlank @Size(
            min = 1,
            max = 255
    ) @Pattern(
            regexp = "[\\P{Cc}]+",
            message = "The propertyName must not contain control characters"
    ) String propertyName;

    @Column(
            name = "PROPERTYVALUE",
            length = 1024
    )
    private @Size(
            min = 0,
            max = 1024
    ) @Pattern(
            regexp = "[\\P{Cc}]+",
            message = "The propertyValue must not contain control characters"
    ) String propertyValue;

    @Column(
            name = "PROPERTYTYPE",
            columnDefinition = "VARCHAR",
            nullable = false
    )
    @Convert(converter = PropertyTypeConverter.class)
    @NotNull
    private PropertyType propertyType;

    @Column(
            name = "DESCRIPTION"
    )
    private @Size(
            max = 255
    ) @Pattern(
            regexp = "[\\P{Cc}]+",
            message = "The description must not contain control characters"
    ) String description;


    public ConfigProperty() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return this.propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public IConfigProperty.PropertyType getPropertyType() {
        return this.propertyType;
    }

    public void setPropertyType(IConfigProperty.PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
