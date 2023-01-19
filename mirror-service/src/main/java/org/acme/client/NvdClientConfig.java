package org.acme.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Dependent
public class NvdClientConfig {

    @Produces
    @ApplicationScoped
    NvdClient nvdClient(@Named("mirrorObjectMapper") final ObjectMapper objectMapper,
                          final NvdConfig config) {
        return new NvdClient(objectMapper, config.api().apiKey().orElse(null));
    }

    @Produces
    @Named("mirrorHttpClient")
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create().build();
    }

    @Produces
    @Named("mirrorObjectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        return objectMapper.registerModule(new JavaTimeModule());
    }

}
