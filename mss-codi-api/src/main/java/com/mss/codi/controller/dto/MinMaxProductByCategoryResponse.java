package com.mss.codi.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mss.codi.config.PriceJsonSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "MinMaxProductByCategoryResponse", description = "최고/최저가격 브랜드 정보")
@Builder
public class MinMaxProductByCategoryResponse {
    @JsonProperty("카테고리")
    private String category;
    @JsonProperty("최저가")
    private List<Brand> minBrands;
    @JsonProperty("최고가")
    private List<Brand> maxBrands;

    @Data
    @Builder
    public static class Brand {
        @JsonProperty("브랜드")
        private String brand;
        @JsonProperty("가격")
        @JsonSerialize(using = PriceJsonSerializer.class)
        private long price;
    }
}
