package com.mss.codi.dao.dto;

import com.mss.codi.type.PriceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopBrandByCategory {
    private PriceType priceType;
    private String brand;
    private long price;
}
