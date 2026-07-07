package com.abcms.circuitbreaker.exchangerate.client;

import com.abcms.circuitbreaker.exchangerate.client.exception.ExternalApiException;
import com.abcms.circuitbreaker.exchangerate.client.response.ExchangeRateApiResponse;
import com.abcms.circuitbreaker.exchangerate.component.OutageSimulator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 환율 API 를 호출하는 클라이언트. 원본의 {@code OrisClient} 처럼 클래스 레벨에
 * {@link CircuitBreaker} 를 붙여, 이 빈의 모든 public 메서드 호출이 CircuitBreaker 로 감싸진다.
 *
 * <p>따라서 이 클래스에는 "실제 원격 호출" 메서드만 둔다. 장애 시뮬레이션 토글 같은 보조 상태는
 * 회로에 걸리지 않도록 {@link OutageSimulator} 로 분리했다.
 *
 * <p>실제 HTTP 연동 대신, 데모를 위해 {@link OutageSimulator} 가 켜지면 {@link ExternalApiException}
 * 을 던지고, 이 예외가 CircuitBreaker 의 실패로 집계된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@CircuitBreaker(name = ExchangeRateClient.CIRCUIT_BREAKER_NAME)
public class ExchangeRateClient {

    public static final String CIRCUIT_BREAKER_NAME = "exchangeRate";

    private final OutageSimulator outageSimulator;

    public ExchangeRateApiResponse fetchRate(String base, String quote) {
        if (outageSimulator.isOutage()) {
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
