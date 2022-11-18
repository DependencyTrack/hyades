package org.acme.client.snyk;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum SeveritySource {

    NVD,

    @JsonProperty("Snyk")
    SNYK,

    @JsonEnumDefaultValue
    UNKNOWN

}
