package com.mss.codi.config;

import com.mss.codi.entity.Product;
import com.mss.codi.service.event.ProductChangeEvent;
import com.mss.codi.service.event.ProductChangeEventBroker;
import com.mss.codi.type.ProductChangeEventType;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductChangeListener {
    private final ProductChangeEventBroker eventBroker;

    @PostPersist
    public void afterPersist(Product product) {
        log.debug("product insert {}", product);
        addEvent(ProductChangeEventType.Created, product);

    }

    @PostUpdate
    public void afterModified(Product product) {
        log.debug("product update {}", product);
        addEvent(ProductChangeEventType.Modified, product);
    }

    @PostRemove
    public void afterRemoved(Product product) {
        log.debug("product delete {}", product);
        addEvent(ProductChangeEventType.Removed, product);
    }

    private void addEvent(ProductChangeEventType type, Product product) {
        eventBroker.addChangedProduct(
                ProductChangeEvent.builder()
                        .type(type)
                        .productId(product.getId())
                        .lastModifiedAt(product.getModifiedAt())
                        .build()
        );
    }
}
