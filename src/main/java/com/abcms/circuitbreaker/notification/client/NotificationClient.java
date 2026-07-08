package com.abcms.circuitbreaker.notification.client;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_NOTIFICATION;
import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.notification.client.exception.NotificationApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 알림 API 클라이언트. 환율과 별개의 CircuitBreaker 인스턴스로 독립 관리된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@CircuitBreaker(name = CIRCUIT_BREAKER_NOTIFICATION)
public class NotificationClient {

    private final OutageSimulator outageSimulator;

    public void send(String message) {
        if (outageSimulator.isDown(CIRCUIT_BREAKER_NOTIFICATION)) {
            log.warn("External notification API call failed (simulated outage): {}", message);
            throw new NotificationApiException("notification API unavailable");
        }
        log.info("External notification API call ok: {}", message);
    }
}
