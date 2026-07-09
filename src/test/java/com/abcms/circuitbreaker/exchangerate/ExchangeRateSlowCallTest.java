package com.abcms.circuitbreaker.exchangerate;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_EXCHANGERATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;
import static org.assertj.core.api.Assertions.assertThat;

import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.exchangerate.client.ExchangeRateClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties =
    "resilience4j.circuitbreaker.instances.exchangeRate.slow-call-duration-threshold=100ms")
@DisplayName("환율 API 서킷 브레이커 - 느린 호출 기반 개방")
class ExchangeRateSlowCallTest {

    private static final int CALLS_TO_OPEN = 5;   // minimum-number-of-calls
    private static final long SLOW_MILLIS = 200;  // threshold(100ms) 보다 느리게

    @Autowired
    private ExchangeRateClient exchangeRateClient;

    @Autowired
    private OutageSimulator externalApi;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void 회로를_초기화한다() {
        circuitBreaker().reset();
        externalApi.recoverAll();
    }

    @Test
    @DisplayName("호출이 실패하지 않아도 느리면 회로가 열린다")
    void slowCallsOpenCircuit() {
        externalApi.slowDown(CIRCUIT_BREAKER_EXCHANGERATE, SLOW_MILLIS);

        for (int i = 0; i < CALLS_TO_OPEN; i++) {
            exchangeRateClient.fetchRate("USD", "KRW"); // 성공하지만 느린 호출
        }

        assertThat(circuitBreaker().getState()).isEqualTo(OPEN);
    }

    private CircuitBreaker circuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_EXCHANGERATE);
    }
}
