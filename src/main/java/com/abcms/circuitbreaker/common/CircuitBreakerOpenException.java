package com.abcms.circuitbreaker.common;

import lombok.Getter;

/**
 * CircuitBreaker 가 OPEN 상태여서 호출이 거부됐을 때 던지는 공통 예외.
 */
@Getter
public class CircuitBreakerOpenException extends RuntimeException {

    private final String circuitBreakerName;

    public CircuitBreakerOpenException(String circuitBreakerName) {
        super("CircuitBreaker '%s' is OPEN - call not permitted".formatted(circuitBreakerName));
        this.circuitBreakerName = circuitBreakerName;
    }
}
