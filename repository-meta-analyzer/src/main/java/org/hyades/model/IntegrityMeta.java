package org.hyades.model;

import java.io.Serializable;
import java.util.Date;

public class IntegrityMeta implements Serializable {

    private String md5;

    private String sha1;

    private String sha256;

    private String sha512;

    private Date currentVersionLastModified;

    private String repositoryUrl;

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public Date getCurrentVersionLastModified() {
        return currentVersionLastModified;
    }

    public void setCurrentVersionLastModified(Date currentVersionLastModified) {
        this.currentVersionLastModified = currentVersionLastModified;
    }

    public void setSha512(String sha512) {
        this.sha512 = sha512;
    }

    public String getSha512() {
        return sha512;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
}