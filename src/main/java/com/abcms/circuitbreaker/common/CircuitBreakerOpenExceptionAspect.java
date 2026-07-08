package com.abcms.circuitbreaker.common;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * {@link io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker} 어노테이션이 붙은 빈에서
 * OPEN 상태로 거부된 호출의 {@link CallNotPermittedException} 을 공통 {@link CircuitBreakerOpenException}으로 변환
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
