package com.abcms.circuitbreaker.exchangerate.client.response;

import java.math.BigDecimal;

/**
 * 외부 환율 API 응답. {@code fromFallback=true} 면 회로 차단/장애로 fallback(마지막 정상값)으로 대체된 응답이다.
 */
public record ExchangeRateApiResponse(String base, String quote, BigDecimal rate, boolean fromFallback) {

    public static ExchangeRateApiResponse live(String base, String quote, BigDecimal rate) {
        return new ExchangeRateApiResponse(base, quote, rate, false);
    }

    public static ExchangeRateApiResponse fromFallback(String base, String quote, BigDecimal rate) {
        return new ExchangeRateApiResponse(base, quote, rate, true);
    }
}
