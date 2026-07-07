package com.abcms.circuitbreaker.exchangerate.component;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

/**
 * 외부 API 장애를 재현해내기 위한 데모용 토글.
 */
@Component
public class OutageSimulator {

    private final AtomicBoolean outage = new AtomicBoolean(false);

    public boolean isOutage() {
        return outage.get();
    }

    public void set(boolean enabled) {
        outage.set(enabled);
    }
}
