package org.hyades.model;

import org.hyades.persistence.model.Component;

import java.io.Serializable;
import java.util.Date;

public class IntegrityModel implements Serializable {

    private Component component;
    private Date publishedTimestamp;
    private boolean md5HashMatched;
    private boolean sha1HashMatched;
    private boolean sha256HashMatched;

    public boolean isMd5HashMatched() {
        return md5HashMatched;
    }

    public void setMd5HashMatched(boolean md5HashMatched) {
        this.md5HashMatched = md5HashMatched;
    }

    public boolean isSha1HashMatched() {
        return sha1HashMatched;
    }

    public void setSha1HashMatched(boolean sha1HashMatched) {
        this.sha1HashMatched = sha1HashMatched;
    }

    public boolean isSha256HashMatched() {
        return sha256HashMatched;
    }

    public void setSha256HashMatched(boolean sha256HashMatched) {
        this.sha256HashMatched = sha256HashMatched;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }


    public Date getPublishedTimestamp() {
        return publishedTimestamp;
    }

    public void setPublishedTimestamp(Date publishedTimestamp) {
        this.publishedTimestamp = publishedTimestamp;
    }

    public IntegrityModel() {
    }

    public IntegrityModel(final Component component) {
        this.component = component;
    }


}
