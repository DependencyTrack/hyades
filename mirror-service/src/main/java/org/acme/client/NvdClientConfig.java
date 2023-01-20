package org.acme.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class NvdClientConfig {

    @Produces
    @ApplicationScoped
    NvdClient nvdClient(@Named("nvdObjectMapper") ObjectMapper objectMapper,
                          NvdConfig config) {
        return new NvdClient(objectMapper, config.api().apiKey().orElse(null));
    }

    @Produces
    @ApplicationScoped
    @Named("nvdObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        return objectMapper.registerModule(new JavaTimeModule());
    }

}
