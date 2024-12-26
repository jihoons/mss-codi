package com.mss.codi.controller;

import com.mss.codi.controller.dto.CodiTargetPriceBrandResponse;
import com.mss.codi.controller.dto.CodiTargetPriceProductAllCategoryResponse;
import com.mss.codi.controller.dto.MinMaxProductByCategoryResponse;
import com.mss.codi.service.CodiCacheService;
import com.mss.codi.type.PriceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/codi")
@Tag(name = "코디 API")
public class CodiController {
    private final CodiCacheService codiCacheService;

    @GetMapping("/all")
    @Operation(description = "카테고리별 최저/최고가격 브랜드와 상품가격, 총액을 조회하는 API")
    public CodiTargetPriceProductAllCategoryResponse getTargetPriceProductByCategory(@Schema(defaultValue = "min", description = "최저가/최고가 구분") @RequestParam(required = false, defaultValue = "min") PriceType.PriceBaseType priceType) {
        return codiCacheService.getTargetPriceByCategory(priceType);
    }

    @GetMapping("/brand")
    @Operation(description = "단일브랜드로 모든 카테고리 코디할 경우 최저/최고가격을 조회하는 API")
    public CodiTargetPriceBrandResponse getTargetPriceBrand(@Schema(defaultValue = "min", description = "최저가/최고가 구분") @RequestParam(required = false, defaultValue = "min") PriceType.PriceBaseType priceType) {
        return codiCacheService.getTargetPriceBrand(priceType);
    }

    @GetMapping("/category")
    @Operation(description = "카테고리 최저,최고 가격브랜드와 상품가격을 조회하는 API")
    public MinMaxProductByCategoryResponse getMinMaxProductByCategory(@Schema(description = "조회할 카테고리") @RequestParam String category) {
        return codiCacheService.getMinMaxProductByCategory(category);
    }
}
