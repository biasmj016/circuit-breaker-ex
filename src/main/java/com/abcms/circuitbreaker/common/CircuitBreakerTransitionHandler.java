package com.abcms.circuitbreaker.common;

/**
 * CircuitBreaker 상태 전이 시 실행할 핸들러
 */
public interface CircuitBreakerTransitionHandler {

    /**
     * CircuitBreaker 인스턴스 이름
     */
    String circuitBreakerName();

    /**
     * CLOSED → OPEN 또는 HALF_OPEN → OPEN 전이 시 호출.
     */
    void onOpen();

    /**
     * HALF_OPEN → CLOSED 전이 시 호출 (외부 회복으로 자동 닫힌 케이스).
     */
    void onClose();
}
