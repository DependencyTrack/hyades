package org.hyades.model;

import org.hyades.persistence.model.Component;
import org.hyades.proto.repointegrityanalysis.v1.HashMatchStatus;

import java.io.Serializable;
import java.util.Date;

public class IntegrityModel implements Serializable {

    private Component component;
    private Date publishedTimestamp;
    private HashMatchStatus hashMatchStatusMd5;
    private HashMatchStatus hashMatchStatusSha1;
    private HashMatchStatus hashMatchStatusSha256;

    public HashMatchStatus getHashMatchStatusMd5() {
        return hashMatchStatusMd5;
    }

    public void setHashMatchStatusMd5(HashMatchStatus hashMatchStatusMd5) {
        this.hashMatchStatusMd5 = hashMatchStatusMd5;
    }

    public HashMatchStatus getHashMatchStatusSha1() {
        return hashMatchStatusSha1;
    }

    public void setHashMatchStatusSha1(HashMatchStatus hashMatchStatusSha1) {
        this.hashMatchStatusSha1 = hashMatchStatusSha1;
    }

    public HashMatchStatus getHashMatchStatusSha256() {
        return hashMatchStatusSha256;
    }

    public void setHashMatchStatusSha256(HashMatchStatus hashMatchStatusSha256) {
        this.hashMatchStatusSha256 = hashMatchStatusSha256;
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
