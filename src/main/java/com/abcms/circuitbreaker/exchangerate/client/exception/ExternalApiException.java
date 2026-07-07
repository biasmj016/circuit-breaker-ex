package com.abcms.circuitbreaker.exchangerate.client.exception;

/**
 * 외부 환율 API 연동 실패 예외.
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }
}
