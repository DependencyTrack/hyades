package org.acme.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.acme.serde.CacheKeyDeserializer;

import java.io.Serializable;
import java.util.Objects;


public class CacheKey implements Serializable {

    String analyzerType;
    String componentPurl;

    public CacheKey(){

    }

    public CacheKey(String analyzerType, String componentPurl) {
        this.analyzerType = analyzerType;
        this.componentPurl = componentPurl;
    }

    public String getAnalyzerType() {
        return analyzerType;
    }

    public void setAnalyzerType(String analyzerType) {
        this.analyzerType = analyzerType;
    }

    public String getComponentPurl() {
        return componentPurl;
    }

    public void setComponentPurl(String componentPurl) {
        this.componentPurl = componentPurl;
    }

    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return analyzerType.equals(cacheKey.analyzerType) && componentPurl.equals(cacheKey.componentPurl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analyzerType, componentPurl);
    }*/
}
