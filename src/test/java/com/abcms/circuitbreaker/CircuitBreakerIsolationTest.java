package com.abcms.circuitbreaker;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_EXCHANGERATE;
import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_NOTIFICATION;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.exchangerate.client.ExchangeRateClient;
import com.abcms.circuitbreaker.notification.client.NotificationClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("여러 서킷 브레이커의 독립성 (환율 · 알림)")
class CircuitBreakerIsolationTest {

    private static final int CALLS_TO_OPEN = 5;
    private static final int HALF_OPEN_PROBES = 3;

    @Autowired
    private ExchangeRateClient exchangeRateClient;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private OutageSimulator outageSimulator;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void 모든_회로를_초기화한다() {
        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_EXCHANGERATE).reset();
        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NOTIFICATION).reset();
        outageSimulator.recoverAll();
    }

    @Test
    @DisplayName("한쪽 외부(환율)만 장애면 그 회로만 열리고, 다른 회로(알림)는 정상 동작한다")
    void opensIndependently() {
        outageSimulator.breakDown(CIRCUIT_BREAKER_EXCHANGERATE);
        failExchangeRateUntilOpen();

        assertThat(state(CIRCUIT_BREAKER_EXCHANGERATE)).isEqualTo(OPEN);
        assertThat(state(CIRCUIT_BREAKER_NOTIFICATION)).isEqualTo(CLOSED);
        assertThatNoException().isThrownBy(() -> notificationClient.send("배포 완료 알림"));
    }

    @Test
    @DisplayName("두 회로가 모두 열려도, 회복된 쪽(알림)만 닫히고 나머지(환율)는 계속 열려 있다")
    void closesIndependently() {
        outageSimulator.breakDown(CIRCUIT_BREAKER_EXCHANGERATE);
        outageSimulator.breakDown(CIRCUIT_BREAKER_NOTIFICATION);
        failExchangeRateUntilOpen();
        failNotificationUntilOpen();
        assertThat(state(CIRCUIT_BREAKER_EXCHANGERATE)).isEqualTo(OPEN);
        assertThat(state(CIRCUIT_BREAKER_NOTIFICATION)).isEqualTo(OPEN);

        // 알림 외부만 회복시켜 알림 회로만 닫는다 (환율은 건드리지 않는다)
        outageSimulator.recover(CIRCUIT_BREAKER_NOTIFICATION);
        circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NOTIFICATION).transitionToHalfOpenState();
        probeNotification(HALF_OPEN_PROBES);

        assertThat(state(CIRCUIT_BREAKER_NOTIFICATION)).isEqualTo(CLOSED);
        assertThat(state(CIRCUIT_BREAKER_EXCHANGERATE)).isEqualTo(OPEN);
    }

    private void failExchangeRateUntilOpen() {
        for (int i = 0; i < CALLS_TO_OPEN; i++) {
            catchThrowable(() -> exchangeRateClient.fetchRate("USD", "KRW"));
        }
    }

    private void failNotificationUntilOpen() {
        for (int i = 0; i < CALLS_TO_OPEN; i++) {
            catchThrowable(() -> notificationClient.send("주문 알림"));
        }
    }

    private void probeNotification(int times) {
        for (int i = 0; i < times; i++) {
            notificationClient.send("probe");
        }
    }

    private State state(String circuitBreakerName) {
        return circuitBreakerRegistry.circuitBreaker(circuitBreakerName).getState();
    }
}
