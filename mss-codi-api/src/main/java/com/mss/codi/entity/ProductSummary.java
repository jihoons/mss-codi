package com.mss.codi.entity;

import com.mss.codi.type.PriceType;

import java.time.LocalDateTime;

public interface ProductSummary {
    Long getProductId();
    long getPrice();
    PriceType getPriceType();
    void setPriceType(PriceType priceType);
    LocalDateTime getModifiedAt();
}
