package com.mss.codi.controller;

import com.mss.codi.controller.dto.CodiTargetPriceBrandResponse;
import com.mss.codi.controller.dto.CodiTargetPriceProductAllCategoryResponse;
import com.mss.codi.controller.dto.MinMaxProductByCategoryResponse;
import com.mss.codi.service.CodiCacheService;
import com.mss.codi.type.PriceType;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class CodiController {
    private final CodiCacheService codiCacheService;

    @GetMapping("/all")
    public CodiTargetPriceProductAllCategoryResponse getTargetPriceProductByCategory(@Schema(defaultValue = "min", description = "최저가/최고가 구분") @RequestParam(required = false, defaultValue = "min") PriceType.PriceBaseType priceType) {
        return codiCacheService.getTargetPriceByCategory(priceType);
    }

    @GetMapping("/brand")
    public CodiTargetPriceBrandResponse getTargetPriceBrand(@Schema(defaultValue = "min", description = "최저가/최고가 구분") @RequestParam(required = false, defaultValue = "min") PriceType.PriceBaseType priceType) {
        return codiCacheService.getTargetPriceBrand(priceType);
    }

    @GetMapping("/category")
    public MinMaxProductByCategoryResponse getMinMaxProductByCategory(@Schema(description = "조회할 카테고리") @RequestParam String category) {
        return codiCacheService.getMinMaxProductByCategory(category);
    }
}
