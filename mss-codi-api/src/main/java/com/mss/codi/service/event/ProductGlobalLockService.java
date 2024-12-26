package com.mss.codi.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductGlobalLockService {
    @Value("${codi.lock.wait:30s}")
    private Duration lockWait;
    private final Map<String, ReentrantLock> categoryLockMap = new ConcurrentHashMap<>();

    public void doInLock(String category, Runnable doSomething, Runnable fallback) {
        if (tryLock(category)) {
            try {
                doSomething.run();
            } finally {
                unlock(category);
            }
        } else {
            fallback.run();
        }
    }

    public boolean tryLock(String category) {
        ReentrantLock lock = categoryLockMap.computeIfAbsent(category, k -> new ReentrantLock());
        try {
            var result = lock.tryLock(lockWait.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("lock {} {}", category, result);
            return result;
        } catch (InterruptedException e) {
            log.error("요약 정보 수정 lock",  e);
            return false;
        }
    }

    public void unlock(String category) {
        ReentrantLock lock = categoryLockMap.get(category);
        if (lock != null) {
            if (lock.isLocked()) {
                log.debug("unlock {}", category);
                lock.unlock();
            } else {
                log.warn("category {} is not locked. It make something bugs.", category);
            }
        }
    }
}
