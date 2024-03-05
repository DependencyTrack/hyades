package org.dependencytrack.common;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dependencytrack.util.WireMockTestResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Suite
@SelectClasses(value = {
        HttpClientTests.HttpClientConfigurationTest.class,
        HttpClientTests.HttpClientConfigWithProxyTest.class,
        HttpClientTests.HttpClientConfigWithNoProxyTest.class,
        HttpClientTests.HttpClientConfigWithNoProxyStarTest.class,
        HttpClientTests.HttpClientConfigWithNoProxyDomainTest.class
})
public class HttpClientTests {
    @QuarkusTest
    @QuarkusTestResource(
            value = WireMockTestResource.class,
            initArgs = @ResourceArg(name = "serverUrlProperty", value = "http://localhost")
    )
    @TestProfile(HttpClientConfigurationTest.TestProfile.class)
    static class HttpClientConfigurationTest {
        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.proxy-timeout-connection", "20",
                        "client.http.config.proxy-timeout-pool", "40",
                        "client.http.config.proxy-timeout-socket", "20"
                );
            }
        }

        @Inject
        HttpClientConfiguration configuration;
        @Inject
        MeterRegistry meterRegistry;
        @WireMockTestResource.InjectWireMock
        WireMockServer wireMockServer;

        @AfterEach
        void afterEach() {
            wireMockServer.resetAll();
        }

        @Test
        void clientCreatedTest() throws IOException {
            try (CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry)) {
                wireMockServer.stubFor(get(urlPathEqualTo("/hello"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .withResponseBody(Body.ofBinaryOrText("hello test".getBytes(),
                                        new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
                HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
                try (CloseableHttpResponse response = client.execute(request)) {
                    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
                    String stringResponse = EntityUtils.toString(response.getEntity());
                    Assertions.assertEquals("hello test", stringResponse);
                } catch (IOException ex) {
                    System.out.println("exception occurred: " + ex.getMessage());
                }

                request = new HttpGet("https://localhost:1080/hello");
                try (CloseableHttpResponse response = client.execute(request)) {
                    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
                    String stringResponse = EntityUtils.toString(response.getEntity());
                    Assertions.assertEquals("hello test", stringResponse);
                } catch (IOException ex) {
                    System.out.println("exception occurred: " + ex.getMessage());
                }
            }
        }
    }

    @QuarkusTest
    @QuarkusTestResource(
            value = WireMockTestResource.class,
            initArgs = @ResourceArg(name = "serverUrlProperty", value = "http://localhost")
    )
    @TestProfile(HttpClientConfigWithProxyTest.TestProfile.class)
    public static class HttpClientConfigWithProxyTest {
        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.proxy-timeout-connection", "20",
                        "client.http.config.proxy-timeout-pool", "40",
                        "client.http.config.proxy-timeout-socket", "20",
                        "client.http.config.proxy-username", "test",
                        "client.http.config.proxy-password", "test",
                        "client.http.config.proxy-address", "http://localhost",
                        "client.http.config.proxy-port", "1080"
                );
            }
        }

        @Inject
        HttpClientConfiguration configuration;
        @Inject
        MeterRegistry meterRegistry;
        @WireMockTestResource.InjectWireMock
        WireMockServer wireMockServer;

        @AfterEach
        void afterEach() {
            wireMockServer.resetAll();
        }

        @Test
        void clientCreatedWithProxyInfoTest() throws IOException {
            try (CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry)) {
                wireMockServer.stubFor(get(urlPathEqualTo("/hello"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf("application/json"))
                                .withResponseBody(Body.ofBinaryOrText("hello test".getBytes(),
                                        new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
                HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
                try (CloseableHttpResponse response = client.execute(request)) {
                    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
                    String stringResponse = EntityUtils.toString(response.getEntity());
                    Assertions.assertEquals("hello test", stringResponse);
                } catch (IOException ex) {
                    System.out.println("exception occurred: " + ex.getMessage());
                }
            }
        }

    }

    @QuarkusTest
    @QuarkusTestResource(
            value = WireMockTestResource.class,
            initArgs = @ResourceArg(name = "serverUrlProperty", value = "http://localhost")
    )
    @TestProfile(HttpClientConfigWithNoProxyTest.TestProfile.class)
    public static class HttpClientConfigWithNoProxyTest {
        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.proxy-timeout-connection", "20",
                        "client.http.config.proxy-timeout-pool", "40",
                        "client.http.config.proxy-timeout-socket", "20",
                        "client.http.config.proxy-username", "test",
                        "client.http.config.proxy-password", "test",
                        "client.http.config.proxy-address", "http://localhost",
                        "client.http.config.proxy-port", "1080",
                        "client.http.config.no-proxy", "http://localhost:8080,*"
                );
            }
        }

        @Inject
        HttpClientConfiguration configuration;
        @Inject
        MeterRegistry meterRegistry;

        @WireMockTestResource.InjectWireMock
        WireMockServer wireMockServer;

        @AfterEach
        void afterEach() {
            wireMockServer.resetAll();
        }

        @Test
        void clientCreatedWithProxyInfoTest() throws IOException {
            try (CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry)) {
                wireMockServer.stubFor(get(urlPathEqualTo("/hello"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf("application/json"))
                                .withResponseBody(Body.ofBinaryOrText("hello test".getBytes(),
                                        new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
                HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
                try (CloseableHttpResponse response = client.execute(request)) {
                    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
                    String stringResponse = EntityUtils.toString(response.getEntity());
                    Assertions.assertEquals("hello test", stringResponse);
                } catch (IOException ex) {
                    System.out.println("exception occurred: " + ex.getMessage());
                }
            }
        }

    }

    @QuarkusTest
    @QuarkusTestResource(
            value = WireMockTestResource.class,
            initArgs = @ResourceArg(name = "serverUrlProperty", value = "http://localhost")
    )
    @TestProfile(HttpClientConfigWithNoProxyStarTest.TestProfile.class)
    public static class HttpClientConfigWithNoProxyStarTest {
        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.proxy-timeout-connection", "20",
                        "client.http.config.proxy-timeout-pool", "40",
                        "client.http.config.proxy-timeout-socket", "20",
                        "client.http.config.proxy-username", "test",
                        "client.http.config.proxy-password", "test",
                        "client.http.config.proxy-address", "http://localhost",
                        "client.http.config.proxy-port", "1080",
                        "client.http.config.no-proxy", "*"
                );
            }
        }

        @Inject
        HttpClientConfiguration configuration;
        @Inject
        MeterRegistry meterRegistry;

        @WireMockTestResource.InjectWireMock
        WireMockServer wireMockServer;

        @AfterEach
        void afterEach() {
            wireMockServer.resetAll();
        }

        @Test
        void clientCreatedWithProxyInfoTest() throws IOException {
            try (CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry)) {
                wireMockServer.stubFor(get(urlPathEqualTo("/hello"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf("application/json"))
                                .withResponseBody(Body.ofBinaryOrText("hello test".getBytes(),
                                        new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
                HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
                try (CloseableHttpResponse response = client.execute(request)) {
                    Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
                    String stringResponse = EntityUtils.toString(response.getEntity());
                    Assertions.assertEquals("hello test", stringResponse);
                } catch (IOException ex) {
                    System.out.println("exception occurred: " + ex.getMessage());
                }
            }
        }

    }

    @QuarkusTest
    @QuarkusTestResource(
            value = WireMockTestResource.class,
            initArgs = @ResourceArg(name = "serverUrlProperty", value = "http://localhost")
    )
    @TestProfile(HttpClientConfigWithNoProxyDomainTest.TestProfile.class)
    public static class HttpClientConfigWithNoProxyDomainTest {
        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.proxy-timeout-connection", "20",
                        "client.http.config.proxy-timeout-pool", "40",
                        "client.http.config.proxy-timeout-socket", "20",
                        "client.http.config.proxy-username", "domain\\test",
                        "client.http.config.proxy-password", "domain#test",
                        "client.http.config.proxy-address", "http://localhost",
                        "client.http.config.proxy-port", "1080"
                );
            }
        }

        @Inject
        HttpClientConfiguration configuration;
        @Inject
        MeterRegistry meterRegistry;

        @WireMockTestResource.InjectWireMock
        WireMockServer wireMockServer;

        @AfterEach
        void afterEach() {
            wireMockServer.resetAll();
        }

        @Test
        void clientCreatedWithProxyInfoTest() throws IOException {
            try (CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry)) {
                wireMockServer.stubFor(get(urlPathEqualTo("/hello"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf("application/json"))
                                .withResponseBody(Body.ofBinaryOrText("hello test".getBytes(),
                                        new ContentTypeHeader("application/json"))).withStatus(HttpStatus.SC_OK)));
                HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
                try (CloseableHttpResponse response = client.execute(request)) {
                    Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                    String stringResponse = EntityUtils.toString(response.getEntity());
                    Assertions.assertEquals("hello test", stringResponse);
                } catch (IOException ex) {
                    System.out.println("exception occurred: " + ex.getMessage());
                }
            }
        }

    }
}
