package org.acme.common;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CircuitBreakerCustomTest {

    @Test
    void testGetCircuitBreaker() {
        CircuitBreakerCustom circuitBreakerCustom = new CircuitBreakerCustom();
        Assertions.assertNotNull(circuitBreakerCustom.getCircuitBreaker());
        Assertions.assertNotNull(circuitBreakerCustom.getCircuitBreaker(CircuitBreakerConfig.ofDefaults()));
    }
}