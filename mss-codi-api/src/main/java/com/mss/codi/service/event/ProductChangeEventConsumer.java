package com.mss.codi.service.event;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductChangeEventConsumer extends Thread implements Runnable {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);

    private final ProductChangeEventBroker productChangeEventBroker;
    private final ApplicationContext applicationContext;
    private Map<String, ProductChangeEventHandler> handlerMap = Collections.emptyMap();

    @Override
    public void run() {
        while (running.get()) {
            var event = productChangeEventBroker.poll();
            if (event != null) {
                log.debug("Received product change event: {}", event);
                processing.set(true);
                callHandler(event);
                processing.set(false);
            }
        }
    }

    private void callHandler(ProductChangeEvent event) {
        handlerMap.forEach((key, value) -> {
            try {
                value.handle(event);
            } catch (Exception e) {
                log.error("Error processing product change event {}, handler: {}", event, key, e);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        while (processing.get()) {
            // 처리중이면 기다린다
            try {
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException ignored) {
            }
        }
        running.set(false);
    }

    @PostConstruct
    public void init() {
        handlerMap = applicationContext.getBeansOfType(ProductChangeEventHandler.class);
        running.set(true);
        start();
    }
}
