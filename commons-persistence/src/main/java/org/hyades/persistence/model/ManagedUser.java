package org.hyades.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MANAGEDUSER")
public class ManagedUser {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "EMAIL")
    private String email;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

}
