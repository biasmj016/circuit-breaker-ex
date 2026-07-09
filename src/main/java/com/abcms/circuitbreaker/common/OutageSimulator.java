package com.abcms.circuitbreaker.common;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 연동별 외부 API 장애를 재현하는 데모용 토글. target(회로 이름)별로 개별 On/Off 되며, 응답 지연(느린 호출)도 주입할 수 있다.
 */
@Component
public class OutageSimulator {

    private final Set<String> downTargets = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> latencyMillis = new ConcurrentHashMap<>();

    public boolean isDown(String target) {
        return downTargets.contains(target);
    }

    public void breakDown(String target) {
        downTargets.add(target);
    }

    public void recover(String target) {
        downTargets.remove(target);
    }

    public long latencyMillis(String target) {
        return latencyMillis.getOrDefault(target, 0L);
    }

    public void slowDown(String target, long millis) {
        latencyMillis.put(target, millis);
    }

    public void recoverAll() {
        downTargets.clear();
        latencyMillis.clear();
    }
}
