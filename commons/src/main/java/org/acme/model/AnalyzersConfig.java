package org.acme.model;

import java.io.Serializable;

public class AnalyzersConfig implements Serializable {
    private boolean snykEnabled;
    private boolean OSSEnabled;
    private boolean internalAnalyzerEnabled;

    public AnalyzersConfig(boolean snykEnabled, boolean OSSEnabled, boolean internalAnalyzerEnabled) {
        this.snykEnabled = snykEnabled;
        this.OSSEnabled = OSSEnabled;
        this.internalAnalyzerEnabled = internalAnalyzerEnabled;
    }

    public AnalyzersConfig(){

    }

    public boolean isSnykEnabled() {
        return snykEnabled;
    }

    public void setSnykEnabled(boolean snykEnabled) {
        this.snykEnabled = snykEnabled;
    }

    public boolean isOSSEnabled() {
        return OSSEnabled;
    }

    public void setOSSEnabled(boolean OSSEnabled) {
        this.OSSEnabled = OSSEnabled;
    }

    public boolean isInternalAnalyzerEnabled() {
        return internalAnalyzerEnabled;
    }

    public void setInternalAnalyzerEnabled(boolean internalAnalyzerEnabled) {
        this.internalAnalyzerEnabled = internalAnalyzerEnabled;
    }
}
