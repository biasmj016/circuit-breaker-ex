package com.abcms.circuitbreaker.exchangerate.component;

import com.abcms.circuitbreaker.common.circuitbreaker.CircuitBreakerTransitionHandler;
import com.abcms.circuitbreaker.exchangerate.client.ExchangeRateClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * exchangeRate CircuitBreaker 의 상태 전이 시 실행할 비즈니스 액션.
 * OPEN 시 회복을 서킷 브레이커 자동복구(HALF_OPEN→CLOSED)에 온전히 맡기므로 별도 킬스위치를 두지 않고, 여기서는 전이 시점을 로그로만 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateCircuitBreakerHandler implements CircuitBreakerTransitionHandler {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public String circuitBreakerName() {
        return ExchangeRateClient.CIRCUIT_BREAKER_NAME;
    }

    @Override
    public void onOpen() {
        log.warn("[exchangeRate CircuitBreaker] OPEN\n{}", buildOpenMessage());
    }

    @Override
    public void onClose() {
        log.info("[exchangeRate CircuitBreaker] CLOSED - auto-recovered\n{}", buildCloseMessage());
    }

    private String buildOpenMessage() {
        Metrics metrics = circuitBreaker().getMetrics();
        return """
            환율 회로가 차단됐습니다.
            - 실패율: %.2f%%
            - 느린호출률: %.2f%%
            - 실패콜수: %d
            - 느린호출수: %d
            - 성공콜수: %d
            - 거부콜수: %d
            - 전체 호출수: %d"""
            .formatted(
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSlowCalls(),
                metrics.getNumberOfSuccessfulCalls(),
                metrics.getNumberOfNotPermittedCalls(),
                metrics.getNumberOfBufferedCalls());
    }

    private String buildCloseMessage() {
        Metrics metrics = circuitBreaker().getMetrics();
        return """
            환율 회로가 자동 회복(CLOSED)됐습니다.
            - 실패율: %.2f%%
            - 성공콜수: %d
            - 실패콜수: %d"""
            .formatted(
                metrics.getFailureRate(),
                metrics.getNumberOfSuccessfulCalls(),
                metrics.getNumberOfFailedCalls());
    }

    private CircuitBreaker circuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker(ExchangeRateClient.CIRCUIT_BREAKER_NAME);
    }
}
