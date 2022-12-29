package org.acme.common;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;

public class CircuitBreakerCustom {

    private CircuitBreakerConfig circuitBreakerConfig;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerCustom() {
        circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(5)
                .minimumNumberOfCalls(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .build();
        circuitBreakerRegistry =
                CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    public CircuitBreaker getCircuitBreaker() {
        // TODO: different instances with different custom config for different clients?
        return circuitBreakerRegistry
                .circuitBreaker("circuitBreakerInstance", circuitBreakerConfig);
    }

    public CircuitBreaker getCircuitBreaker(CircuitBreakerConfig config) {
        CircuitBreakerRegistry circuitBreakerRegistry
                = CircuitBreakerRegistry.ofDefaults();
        return circuitBreakerRegistry
                .circuitBreaker("circuitBreakerDefault", config);
    }
}