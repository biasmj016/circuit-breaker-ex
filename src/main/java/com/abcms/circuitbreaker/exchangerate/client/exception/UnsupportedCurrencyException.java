package com.abcms.circuitbreaker.exchangerate.client.exception;

public class UnsupportedCurrencyException extends RuntimeException {

    public UnsupportedCurrencyException(String message) {
        super(message);
    }
}
