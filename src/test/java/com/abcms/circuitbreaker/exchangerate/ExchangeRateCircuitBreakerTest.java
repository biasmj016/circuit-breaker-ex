package com.abcms.circuitbreaker.exchangerate;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_EXCHANGERATE;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.HALF_OPEN;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.common.CircuitBreakerOpenException;
import com.abcms.circuitbreaker.exchangerate.client.ExchangeRateClient;
import com.abcms.circuitbreaker.exchangerate.client.response.ExchangeRateApiResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("환율 API 서킷 브레이커")
class ExchangeRateCircuitBreakerTest {

    private static final int CALLS_TO_OPEN = 5;
    private static final int HALF_OPEN_PROBES = 3;

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

    @Nested
    @DisplayName("외부 API 가 정상일 때 (CLOSED)")
    class WhenHealthy {

        @Test
        @DisplayName("환율을 그대로 반환하고 회로는 닫힌 채 유지된다")
        void passesThrough() {
            ExchangeRateApiResponse response = exchangeRateClient.fetchRate("USD", "KRW");

            assertThat(response.rate()).isEqualByComparingTo("1385.20");
            assertThat(state()).isEqualTo(CLOSED);
        }
    }

    @Nested
    @DisplayName("외부 API 실패가 임계치를 넘으면 (CLOSED → OPEN)")
    class WhenFailing {

        @BeforeEach
        void 외부_장애가_임계치까지_이어진다() {
            externalApi.breakDown(CIRCUIT_BREAKER_EXCHANGERATE);
            failUntilOpen();
        }

        @Test
        @DisplayName("회로가 열린다")
        void opens() {
            assertThat(state()).isEqualTo(OPEN);
        }

        @Test
        @DisplayName("이후 호출은 외부로 나가지 않고 공통 예외로 즉시 차단된다")
        void rejectsFastWithCommonException() {
            assertThatThrownBy(() -> exchangeRateClient.fetchRate("USD", "KRW"))
                .isInstanceOf(CircuitBreakerOpenException.class)
                .hasMessageContaining(CIRCUIT_BREAKER_EXCHANGERATE);
        }
    }

    @Nested
    @DisplayName("열린 회로에서 외부가 회복되면 (OPEN → HALF_OPEN → CLOSED)")
    class WhenRecovering {

        @Test
        @DisplayName("프로브가 성공하면 사람 개입 없이 스스로 닫힌다")
        void closesItself() {
            externalApi.breakDown(CIRCUIT_BREAKER_EXCHANGERATE);
            failUntilOpen();

            externalApi.recover(CIRCUIT_BREAKER_EXCHANGERATE);
            circuitBreaker().transitionToHalfOpenState();
            assertThat(state()).isEqualTo(HALF_OPEN);

            probe(HALF_OPEN_PROBES);

            assertThat(state()).isEqualTo(CLOSED);
        }
    }

    private void failUntilOpen() {
        for (int i = 0; i < CALLS_TO_OPEN; i++) {
            catchThrowable(() -> exchangeRateClient.fetchRate("USD", "KRW"));
        }
    }

    private void probe(int times) {
        for (int i = 0; i < times; i++) {
            exchangeRateClient.fetchRate("USD", "KRW");
        }
    }

    private CircuitBreaker circuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_EXCHANGERATE);
    }

    private State state() {
        return circuitBreaker().getState();
    }
}
