package com.abcms.circuitbreaker.notification.component;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_NOTIFICATION;
import com.abcms.circuitbreaker.common.CircuitBreakerTransitionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * notification CircuitBreaker 의 상태 전이 핸들러
 */
@Slf4j
@Component
public class NotificationCircuitBreakerHandler implements CircuitBreakerTransitionHandler {

    @Override
    public String circuitBreakerName() {
        return CIRCUIT_BREAKER_NOTIFICATION;
    }

    @Override
    public void onOpen() {
        log.warn("[notification CircuitBreaker] OPEN - 알림 회로가 차단됐습니다.");
    }

    @Override
    public void onClose() {
        log.info("[notification CircuitBreaker] CLOSED - 알림 회로가 자동 회복됐습니다.");
    }
}
