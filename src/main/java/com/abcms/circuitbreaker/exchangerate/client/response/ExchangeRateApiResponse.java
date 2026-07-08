package com.abcms.circuitbreaker.exchangerate.client.response;

import java.math.BigDecimal;

/**
 * 외부 환율 API 응답
 */
public record ExchangeRateApiResponse(String base, String quote, BigDecimal rate) {
}
