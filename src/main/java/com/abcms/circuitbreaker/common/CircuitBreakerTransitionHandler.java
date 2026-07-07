package com.abcms.circuitbreaker.common.circuitbreaker;

/**
 * CircuitBreaker 상태 전이 시 실행할 비즈니스 액션을 캡슐화한다.
 *
 * <p>외부 연동마다 1 구현체를 두면 {@link CircuitBreakerTransitionDispatcher} 가 자동으로 구독한다.
 * 신규 외부 연동 추가 시 yml 의 인스턴스 정의 + 이 인터페이스 구현체 1 개만 작성하면 된다.
 */
public interface CircuitBreakerTransitionHandler {

    /**
     * 구독 대상 CircuitBreaker 인스턴스 이름
     * (yml {@code resilience4j.circuitbreaker.instances.~} 와 일치해야 한다).
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
