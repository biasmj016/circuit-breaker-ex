package com.abcms.circuitbreaker.common.circuitbreaker;

import lombok.Getter;

/**
 * CircuitBreaker 가 OPEN 상태여서 호출이 거부됐을 때 던지는 공통 예외.
 *
 * <p>원본 프로젝트에서는 사내 {@code CustomException}/{@code ErrorType} 계층을 상속했지만,
 * 토이 프로젝트에는 그 인프라가 없으므로 독립적인 {@link RuntimeException} 으로 단순화했다.
 *
 * <p>호출처는 외부 연동마다 다른 {@code CallNotPermittedException} 을 직접 잡지 않고
 * 이 단일 예외만 처리하면 된다.
 */
@Getter
public class CircuitBreakerOpenException extends RuntimeException {

    private final String circuitBreakerName;

    public CircuitBreakerOpenException(String circuitBreakerName) {
        super("CircuitBreaker '%s' is OPEN - call not permitted".formatted(circuitBreakerName));
        this.circuitBreakerName = circuitBreakerName;
    }
}
