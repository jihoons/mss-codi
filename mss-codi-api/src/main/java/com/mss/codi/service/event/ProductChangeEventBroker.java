package com.mss.codi.service.event;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductChangeEventBroker {
    private final BlockingQueue<ProductChangeEvent> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean requestedStop = new AtomicBoolean(false);

    public ProductChangeEvent poll() {
        return queue.poll();
    }

    public void addChangedProduct(ProductChangeEvent event) {
        if (requestedStop.get())
            return;

        queue.add(event);
    }

    @PreDestroy
    public void destroy() {
        requestedStop.set(true);
    }
}
