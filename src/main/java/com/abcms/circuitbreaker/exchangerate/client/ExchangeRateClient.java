package com.abcms.circuitbreaker.exchangerate.client;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_EXCHANGERATE;
import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.exchangerate.client.exception.ExternalApiException;
import com.abcms.circuitbreaker.exchangerate.client.exception.UnsupportedCurrencyException;
import com.abcms.circuitbreaker.exchangerate.client.response.ExchangeRateApiResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 환율 API 를 호출하는 클라이언트 (데모). 회로가 열리면 예외 대신 마지막 정상값으로 degrade 한다 ({@code fallbackMethod}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@CircuitBreaker(name = CIRCUIT_BREAKER_EXCHANGERATE, fallbackMethod = "fallback")
public class ExchangeRateClient {

    private final OutageSimulator outageSimulator;
    private final Map<String, BigDecimal> lastKnownRate = new ConcurrentHashMap<>();

    public ExchangeRateApiResponse fetchRate(String base, String quote) {
        if (outageSimulator.isDown(CIRCUIT_BREAKER_EXCHANGERATE)) {
            log.warn("External exchange-rate API call failed (simulated outage): {}->{}", base, quote);
            throw new ExternalApiException("exchange-rate API unavailable: %s->%s".formatted(base, quote));
        }
        BigDecimal rate = mockRate(base, quote);
        simulateLatency();
        lastKnownRate.put(pair(base, quote), rate);
        log.info("External exchange-rate API call ok: {}->{} = {}", base, quote, rate);
        return ExchangeRateApiResponse.live(base, quote, rate);
    }

    /** 외부 인프라 실패 시 degrade. */
    private ExchangeRateApiResponse fallback(String base, String quote, ExternalApiException e) {
        return degraded(base, quote);
    }

    /** 회로가 OPEN 이라 호출이 거부됐을 때 degrade. */
    private ExchangeRateApiResponse fallback(String base, String quote, CallNotPermittedException e) {
        return degraded(base, quote);
    }

    private ExchangeRateApiResponse degraded(String base, String quote) {
        BigDecimal cached = lastKnownRate.get(pair(base, quote));
        log.warn("exchange-rate degraded to last-known value: {}->{} = {}", base, quote, cached);
        return ExchangeRateApiResponse.stale(base, quote, cached);
    }

    private BigDecimal mockRate(String base, String quote) {
        return switch (pair(base, quote)) {
            case "USD/KRW" -> BigDecimal.valueOf(1385.20);
            case "EUR/KRW" -> BigDecimal.valueOf(1502.75);
            case "JPY/KRW" -> BigDecimal.valueOf(9.12);
            default -> throw new UnsupportedCurrencyException("unsupported currency pair: " + pair(base, quote));
        };
    }

    private void simulateLatency() {
        long millis = outageSimulator.latencyMillis(CIRCUIT_BREAKER_EXCHANGERATE);
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String pair(String base, String quote) {
        return (base + "/" + quote).toUpperCase();
    }
}
