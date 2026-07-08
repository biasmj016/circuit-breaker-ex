package com.abcms.circuitbreaker.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class CircuitBreakerNameConstants {
    public static final String CIRCUIT_BREAKER_NOTIFICATION = "notification";
    public static final String CIRCUIT_BREAKER_EXCHANGERATE = "exchangeRate";
}
