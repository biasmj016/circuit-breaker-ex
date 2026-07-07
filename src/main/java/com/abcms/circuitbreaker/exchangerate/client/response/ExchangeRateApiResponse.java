package com.abcms.circuitbreaker.exchangerate.client.response;

import java.math.BigDecimal;

/**
 * 외부 환율 API 의 원시 응답 (client 레이어 경계).
 *
 * <p>실제 연동이라면 외부 JSON 스펙에 맞춘 역직렬화 대상이 된다.
 */
public record ExchangeRateApiResponse(String base, String quote, BigDecimal rate) {
}
