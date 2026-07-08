package com.abcms.circuitbreaker.common;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 등록된 모든 {@link CircuitBreakerTransitionHandler} 구현체를 {@link CircuitBreakerRegistry} 의 상태전이 이벤트에 자동 등록.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "resilience4j.transition-dispatcher.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class CircuitBreakerTransitionDispatcher {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final List<CircuitBreakerTransitionHandler> handlers;

    @PostConstruct
    void subscribe() {
        for (CircuitBreakerTransitionHandler handler : handlers) {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(handler.circuitBreakerName());
            circuitBreaker.getEventPublisher().onStateTransition(event -> handle(handler, event.getStateTransition().getToState()));
            log.info("CircuitBreakerTransitionHandler subscribed: name={}", handler.circuitBreakerName());
        }
    }

    private void handle(CircuitBreakerTransitionHandler handler, State toState) {
        if (toState == State.OPEN) {
            log.error("CircuitBreaker OPEN: name={}", handler.circuitBreakerName());
            handler.onOpen();
        } else if (toState == State.CLOSED) {
            log.info("CircuitBreaker CLOSED: name={}", handler.circuitBreakerName());
            handler.onClose();
        }
    }
}
