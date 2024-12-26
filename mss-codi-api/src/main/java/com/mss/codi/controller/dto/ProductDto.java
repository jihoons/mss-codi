package com.mss.codi.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

public class ProductDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewProduct {
        @NotEmpty(message = "브랜드를 지정해주세요.")
        private String brand;
        @NotEmpty(message = "카테고리를 지정해주세요.")
        private String category;
        @Min(value = 1, message = "가격을 확인해주세요.")
        private long price;
    }

    @EqualsAndHashCode(callSuper = false)
    @Data
    @NoArgsConstructor
//    @AllArgsConstructor
    public static class Product extends NewProduct {
        @Min(value = 1, message = "상품ID를 확인해주세요.")
        private long id;

        @Builder
        public Product(long id, String brand, String category, long price) {
            super(brand, category, price);
            this.id = id;
        }
    }
}
