package com.abcms.circuitbreaker.exchangerate.client;

import static com.abcms.circuitbreaker.common.CircuitBreakerNameConstants.CIRCUIT_BREAKER_EXCHANGERATE;
import com.abcms.circuitbreaker.common.OutageSimulator;
import com.abcms.circuitbreaker.exchangerate.client.exception.ExternalApiException;
import com.abcms.circuitbreaker.exchangerate.client.response.ExchangeRateApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 환율 API 를 호출하는 클라이언트 (데모)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@CircuitBreaker(name = CIRCUIT_BREAKER_EXCHANGERATE)
public class ExchangeRateClient {

    private final OutageSimulator outageSimulator;

    public ExchangeRateApiResponse fetchRate(String base, String quote) {
        if (outageSimulator.isDown(CIRCUIT_BREAKER_EXCHANGERATE)) {
            log.warn("External exchange-rate API call failed (simulated outage): {}->{}", base, quote);
            throw new ExternalApiException("exchange-rate API unavailable: %s->%s".formatted(base, quote));
        }
        BigDecimal rate = mockRate(base, quote);
        log.info("External exchange-rate API call ok: {}->{} = {}", base, quote, rate);
        return new ExchangeRateApiResponse(base, quote, rate);
    }

    private BigDecimal mockRate(String base, String quote) {
        String pair = (base + "/" + quote).toUpperCase();
        return switch (pair) {
            case "USD/KRW" -> BigDecimal.valueOf(1385.20);
            case "EUR/KRW" -> BigDecimal.valueOf(1502.75);
            case "JPY/KRW" -> BigDecimal.valueOf(9.12);
            default -> BigDecimal.valueOf(1000.00);
        };
    }
}
