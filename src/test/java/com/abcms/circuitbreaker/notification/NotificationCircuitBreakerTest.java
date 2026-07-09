package com.abcms.circuitbreaker.notification;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_NOTIFICATION;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.HALF_OPEN;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.abcms.circuitbreaker.common.CircuitBreakerOpenException;
import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.notification.client.NotificationClient;
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
@DisplayName("알림 API 서킷 브레이커 (커맨드 → 회로 열리면 fail-fast)")
class NotificationCircuitBreakerTest {

    private static final int CALLS_TO_OPEN = 5;      // minimum-number-of-calls
    private static final int HALF_OPEN_PROBES = 3;   // permitted-number-of-calls-in-half-open-state

    @Autowired
    private NotificationClient notificationClient;

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
        @DisplayName("알림을 전송하고 회로는 닫힌 채 유지된다")
        void sends() {
            assertThatNoException().isThrownBy(() -> notificationClient.send("주문 완료"));
            assertThat(state()).isEqualTo(CLOSED);
        }
    }

    @Nested
    @DisplayName("외부 API 실패가 임계치를 넘으면 (CLOSED → OPEN)")
    class WhenFailing {

        @BeforeEach
        void 외부_장애가_임계치까지_이어진다() {
            externalApi.breakDown(CIRCUIT_BREAKER_NOTIFICATION);
            failUntilOpen();
        }

        @Test
        @DisplayName("회로가 열린다")
        void opens() {
            assertThat(state()).isEqualTo(OPEN);
        }

        @Test
        @DisplayName("degrade 할 수 없는 커맨드이므로 공통 예외로 즉시 차단된다")
        void rejectsFastWithCommonException() {
            assertThatThrownBy(() -> notificationClient.send("주문 완료"))
                .isInstanceOf(CircuitBreakerOpenException.class)
                .hasMessageContaining(CIRCUIT_BREAKER_NOTIFICATION);
        }
    }

    @Nested
    @DisplayName("외부가 회복되면 (OPEN → HALF_OPEN → CLOSED)")
    class WhenRecovering {

        @Test
        @DisplayName("프로브가 성공하면 사람 개입 없이 스스로 닫힌다")
        void closesItself() {
            externalApi.breakDown(CIRCUIT_BREAKER_NOTIFICATION);
            failUntilOpen();

            externalApi.recover(CIRCUIT_BREAKER_NOTIFICATION);
            circuitBreaker().transitionToHalfOpenState();
            assertThat(state()).isEqualTo(HALF_OPEN);

            probe(HALF_OPEN_PROBES);

            assertThat(state()).isEqualTo(CLOSED);
        }
    }

    private void failUntilOpen() {
        for (int i = 0; i < CALLS_TO_OPEN; i++) {
            catchThrowable(() -> notificationClient.send("주문 완료"));
        }
    }

    private void probe(int times) {
        for (int i = 0; i < times; i++) {
            notificationClient.send("주문 완료");
        }
    }

    private CircuitBreaker circuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NOTIFICATION);
    }

    private State state() {
        return circuitBreaker().getState();
    }
}
