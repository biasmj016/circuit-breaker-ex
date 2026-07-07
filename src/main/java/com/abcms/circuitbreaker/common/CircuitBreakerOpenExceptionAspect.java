package com.abcms.circuitbreaker.common.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * {@link io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker} 어노테이션이 붙은 빈에서
 * OPEN 상태로 거부된 호출의 {@link CallNotPermittedException} 을 공통 {@link CircuitBreakerOpenException}
 * 으로 변환한다.
 *
 * <p>resilience4j 의 CircuitBreaker AOP 보다 바깥(outer)에서 동작해야 {@code CallNotPermittedException}
 * 을 가로챌 수 있으므로 {@link Ordered#HIGHEST_PRECEDENCE} 로 둔다.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CircuitBreakerOpenExceptionAspect {

    @Around("@within(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker) "
        + "|| @annotation(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker)")
    public Object translate(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (CallNotPermittedException ex) {
            throw new CircuitBreakerOpenException(ex.getCausingCircuitBreakerName());
        }
    }
}
