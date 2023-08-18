package org.hyades.model;

import org.hyades.persistence.model.Component;
import org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus;

import java.io.Serializable;
import java.util.Date;

public class IntegrityModel implements Serializable {

    private Component component;
    private Date publishedTimestamp;
    private HashMatchStatus md5HashMatched;
    private HashMatchStatus sha1HashMatched;
    private HashMatchStatus sha256HashMatched;

    public HashMatchStatus isMd5HashMatched() {
        return md5HashMatched;
    }

    public void setMd5HashMatched(HashMatchStatus md5HashMatched) {
        this.md5HashMatched = md5HashMatched;
    }

    public HashMatchStatus isSha1HashMatched() {
        return sha1HashMatched;
    }

    public void setSha1HashMatched(HashMatchStatus sha1HashMatched) {
        this.sha1HashMatched = sha1HashMatched;
    }

    public HashMatchStatus isSha256HashMatched() {
        return sha256HashMatched;
    }

    public void setSha256HashMatched(HashMatchStatus sha256HashMatched) {
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
