package com.mss.codi.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mss.codi.config.PriceJsonSerializer;
import com.mss.codi.entity.CategoryProductSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "CodiMinPriceByCategoryResponse", description = "카테고리별 브랜드 가격 정보")
@Builder
public class CodiTargetPriceProductAllCategoryResponse {
    @JsonProperty("카테고리별상품")
    @Builder.Default
    private List<BrandOfCategory> targetBrandOfCategories = new ArrayList<>();
    @JsonProperty("총액")
    @JsonSerialize(using = PriceJsonSerializer.class)
    private long totalPrice;

    @Data
    @Schema(name = "CodiMinPriceByCategoryResponse.Product", description = "카테고리별 브랜드 가격 정보")
    @Builder
    public static class BrandOfCategory {
        @JsonProperty("카테고리")
        private String category;
        @JsonProperty("브랜드")
        private String brand;
        @JsonProperty("가격")
        @JsonSerialize(using = PriceJsonSerializer.class)
        private long price;
    }

    public void addBrandOfCategory(BrandOfCategory brandOfCategory) {
        this.targetBrandOfCategories.add(brandOfCategory);
        this.totalPrice += brandOfCategory.getPrice();
    }
}
