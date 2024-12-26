package com.mss.codi.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mss.codi.config.PriceJsonSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "CodiTargetPriceBrandResponse", description = "최고/최저가격 코디 브랜드 정보")
@Builder
public class CodiTargetPriceBrandResponse {
    @JsonProperty("최저가")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Brand minBrand;
    @JsonProperty("최고가")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Brand maxBrand;

    @JsonIgnore
    public Brand getBrand() {
        if (minBrand != null) {
            return minBrand;
        }
        return maxBrand;
    }

    @Data
    @Builder
    public static class Brand {
        @JsonProperty("브랜드")
        private String brand;
        @JsonProperty("카테고리")
        @Builder.Default
        private List<PriceOfCategory> categories = new ArrayList<>();
        @JsonProperty("총액")
        @JsonSerialize(using = PriceJsonSerializer.class)
        private long totalPrice;

        public void addProduct(PriceOfCategory category) {
            categories.add(category);
            totalPrice += category.getPrice();
        }
    }

    @Data
    @Builder
    public static class PriceOfCategory {
        @JsonProperty("카테고리")
        private String category;
        @JsonProperty("가격")
        @JsonSerialize(using = PriceJsonSerializer.class)
        private long price;
    }
}
