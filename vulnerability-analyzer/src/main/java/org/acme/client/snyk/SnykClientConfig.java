package org.acme.client.snyk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Dependent
public class SnykClientConfig {

    @Produces
    @Named("snykHttpClient")
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }

    @Produces
    @Named("snykObjectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

}
