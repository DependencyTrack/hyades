package org.acme.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * A Quarkus test resource that provisions a WireMock server on a random open port.
 * <p>
 * Note that the same {@link WireMockServer} instance will be used for all tests in the
 * annotated test class. Stubs will need to manually be reset after each test using {@link WireMockServer#resetAll()}.
 *
 * @see <a href="https://quarkus.io/guides/getting-started-testing#altering-the-test-class">Quarkus Documentation</a>
 */
public class WireMockTestResource implements QuarkusTestResourceLifecycleManager {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InjectWireMock {
    }

    private WireMockServer wireMockServer;
    private String serverUrlProperty;

    @Override
    public void init(final Map<String, String> initArgs) {
        serverUrlProperty = initArgs.get("serverUrlProperty");
    }

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();

        if (serverUrlProperty == null) {
            return null;
        }

        return Map.of(serverUrlProperty, wireMockServer.baseUrl());
    }

    @Override
    public synchronized void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @Override
    public void inject(final TestInjector testInjector) {
        testInjector.injectIntoFields(wireMockServer,
                new TestInjector.AnnotatedAndMatchesType(InjectWireMock.class, WireMockServer.class));
    }

}
