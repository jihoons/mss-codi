package com.mss.codi.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

public class ProductDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ProductDto.NewProduct", description = "추가할 상품 정보")
    public static class NewProduct {
        @NotEmpty(message = "브랜드를 지정해주세요.")
        @Schema(name = "brand", description = "브랜드")
        private String brand;
        @NotEmpty(message = "카테고리를 지정해주세요.")
        @Schema(name = "category", description = "카테고리")
        private String category;
        @Min(value = 1, message = "가격을 확인해주세요.")
        @Schema(name = "price", description = "가격")
        private long price;
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    @NoArgsConstructor
    public static class Product extends NewProduct {
        @Min(value = 1, message = "상품ID를 확인해주세요.")
        @Schema(name = "id", description = "상품 ID")
        private long id;

        @Builder
        public Product(long id, String brand, String category, long price) {
            super(brand, category, price);
            this.id = id;
        }
    }
}
