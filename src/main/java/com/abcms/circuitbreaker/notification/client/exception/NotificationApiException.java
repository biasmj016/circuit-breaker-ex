package com.abcms.circuitbreaker.notification.client.exception;

/**
 * 외부 알림 API 연동 실패 예외.
 */
public class NotificationApiException extends RuntimeException {

    public NotificationApiException(String message) {
        super(message);
    }
}
