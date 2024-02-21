package org.dependencytrack.notification.publisher;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.json.JsonObjectBuilder;
import org.dependencytrack.notification.util.WireMockTestResource;
import org.dependencytrack.persistence.model.ConfigPropertyConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@QuarkusTestResource(WireMockTestResource.class)
abstract class AbstractWebhookPublisherTest<T extends AbstractWebhookPublisher> extends AbstractPublisherTest<T> {

    @WireMockTestResource.InjectWireMock
    WireMockServer wireMockServer;

    @BeforeEach
    void beforeEach() {
        wireMockServer.stubFor(post(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    @AfterEach
    void afterEach() {
        wireMockServer.resetAll();
    }

    @Override
    JsonObjectBuilder extraConfig() {
        return super.extraConfig()
                .add(Publisher.CONFIG_DESTINATION, wireMockServer.baseUrl());
    }

}
