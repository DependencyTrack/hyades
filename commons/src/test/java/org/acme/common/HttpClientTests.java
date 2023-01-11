package org.acme.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

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
    @TestProfile(HttpClientConfigurationTest.TestProfile.class)
    public static class HttpClientConfigurationTest {
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
        private static ClientAndServer mockServer;

        @BeforeAll
        public static void beforeClass() {
            mockServer = ClientAndServer.startClientAndServer(1080);
        }

        @AfterAll
        public static void afterClass() {
            mockServer.stop();
        }


        @Test
         void clientCreatedTest() {
            CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry);
            new MockServerClient("localhost", mockServer.getPort())
                    .when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/hello")
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .withBody("hello test")
                    );
            HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
            try {
                CloseableHttpResponse response = client.execute(request);
                Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }

            request = new HttpGet("https://localhost:1080/hello");
            try {
                CloseableHttpResponse response = client.execute(request);
                Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }
        }
    }

    @QuarkusTest
    @TestProfile(HttpClientConfigWithProxyTest.TestProfile.class)
    public static class HttpClientConfigWithProxyTest{
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
        private static ClientAndServer mockServer;

        @BeforeAll
        public static void beforeClass() {
            mockServer = ClientAndServer.startClientAndServer(1080);
        }

        @AfterAll
        public static void afterClass() {
            mockServer.stop();
        }

        @Test
        void clientCreatedWithProxyInfoTest() {
            CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry);
            new MockServerClient("localhost", mockServer.getPort())
                    .when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/hello")
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .withBody("hello test")
                    );
            HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
            try {
                CloseableHttpResponse response = client.execute(request);
                Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }
        }

    }

    @QuarkusTest
    @TestProfile(HttpClientConfigWithNoProxyTest.TestProfile.class)
    public static class HttpClientConfigWithNoProxyTest{
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
        private static ClientAndServer mockServer;

        @BeforeAll
        public static void beforeClass() {
            mockServer = ClientAndServer.startClientAndServer(1080);
        }

        @AfterAll
        public static void afterClass() {
            mockServer.stop();
        }

        @Test
        void clientCreatedWithProxyInfoTest() {
            CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry);
            new MockServerClient("localhost", mockServer.getPort())
                    .when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/hello")
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .withBody("hello test")
                    );
            HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
            try {
                CloseableHttpResponse response = client.execute(request);
                Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }
        }

    }

    @QuarkusTest
    @TestProfile(HttpClientConfigWithNoProxyStarTest.TestProfile.class)
    public static class HttpClientConfigWithNoProxyStarTest{
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
        private static ClientAndServer mockServer;

        @BeforeAll
        public static void beforeClass() {
            mockServer = ClientAndServer.startClientAndServer(1080);
        }

        @AfterAll
        public static void afterClass() {
            mockServer.stop();
        }

        @Test
        void clientCreatedWithProxyInfoTest() {
            CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry);
            new MockServerClient("localhost", mockServer.getPort())
                    .when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/hello")
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .withBody("hello test")
                    );
            HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
            try {
                CloseableHttpResponse response = client.execute(request);
                Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }
        }

    }

    @QuarkusTest
    @TestProfile(HttpClientConfigWithNoProxyDomainTest.TestProfile.class)
    public static class HttpClientConfigWithNoProxyDomainTest{
        public static class TestProfile implements QuarkusTestProfile {
            @Override
            public Map<String, String> getConfigOverrides() {
                return Map.of(
                        "client.http.config.proxy-timeout-connection", "20",
                        "client.http.config.proxy-timeout-pool", "40",
                        "client.http.config.proxy-timeout-socket", "20",
                        "client.http.config.proxy-username", "domain\test",
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
        private static ClientAndServer mockServer;

        @BeforeAll
        public static void beforeClass() {
            mockServer = ClientAndServer.startClientAndServer(1080);
        }

        @AfterAll
        public static void afterClass() {
            mockServer.stop();
        }

        @Test
        void clientCreatedWithProxyInfoTest() {
            CloseableHttpClient client = configuration.newManagedHttpClient(meterRegistry);
            new MockServerClient("localhost", mockServer.getPort())
                    .when(
                            request()
                                    .withMethod("GET")
                                    .withPath("/hello")
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                    .withBody("hello test")
                    );
            HttpUriRequest request = new HttpGet("http://localhost:1080/hello");
            try {
                CloseableHttpResponse response = client.execute(request);
                Assertions.assertEquals(200, response.getStatusLine().getStatusCode());
                String stringResponse = EntityUtils.toString(response.getEntity());
                Assertions.assertEquals("hello test", stringResponse);
            } catch (IOException ex) {
                System.out.println("exception occurred: " + ex.getMessage());
            }
        }

    }
}
