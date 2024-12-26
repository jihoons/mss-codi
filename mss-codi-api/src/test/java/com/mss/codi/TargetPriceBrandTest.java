package com.mss.codi;

import com.mss.codi.controller.dto.CodiTargetPriceBrandResponse;
import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.service.CategoryService;
import com.mss.codi.service.CodiService;
import com.mss.codi.service.ProductService;
import com.mss.codi.type.PriceType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(classes = Application.class)
@TestExecutionListeners({DataResetTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@ActiveProfiles(value = "test")
public class TargetPriceBrandTest {
    @Autowired
    private CodiService codiService;
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductModifyService productModifyService;

    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("단일브랜드 최저/최고가 조회")
    public void testMinMaxBrand() {
        var categories = categoryService.getAllCategories();
        testMinMaxBrand(categories.size());
    }

    private void testMinMaxBrand(int categoryCount) {
        var brandCategory = getBrandCategoryMinMax(categoryCount);
        var min = brandCategory.getLeft();
        var max = brandCategory.getRight();

        testTopBrand(min, PriceType.PriceBaseType.Min);
        testTopBrand(max, PriceType.PriceBaseType.Max);
    }

    @Test
    @DisplayName("상품 변경 후 단일브랜드 최저/최고가 조회")
    public void testAfterProductModifiedMinMaxBrand() {
        var categories = categoryService.getAllCategories();

        var newProduct = new ProductDto.NewProduct("D", "상의", 100000L);
        var newMaxProduct = new ProductDto.NewProduct("F", "상의", 10L);
        var productModifyContext = productModifyService.addProduct(List.of(newProduct, newMaxProduct));
        log.info("상품 추가 후 검증");
        testMinMaxBrand(categories.size());

        // 삭제 후 검증
        productModifyService.removeProduct(productModifyContext);
        log.info("상품 삭제 후 검증");
        testMinMaxBrand(categories.size());

        // 수정 후 검증
        // 가격 수정
        log.info("상품 가격 변경 후 검증");
        var optProduct = productModifyService.findProduct("상의", "D");
        Assertions.assertTrue(optProduct.isPresent());
        var product = optProduct.get();

        productModifyService.modifyProductPrice(product, 200000L);
        testMinMaxBrand(categories.size());

        // 카테고리 수정
        log.info("상품 카테고리 변경 후 검증");
        productModifyService.modifyProductCategory(product, "양말");
        testMinMaxBrand(categories.size());

        // 브랜드 수정
        log.info("상품 브랜드 변경 후 검증");
        productModifyService.modifyProductBrand(product, "C");
        testMinMaxBrand(categories.size());
    }

    private void testTopBrand(BrandSummary expected, PriceType.PriceBaseType priceBaseType) {
        var response = codiService.getTargetPriceBrand(priceBaseType);
        log.info("단일 브랜드 {} 조회\n{}", priceBaseType, response);

        Assertions.assertNotNull(response);
        var brand = response.getBrand();
        Assertions.assertNotNull(brand);

        log.info("brand expected: {}, actual: {}", expected.getBrand(), brand.getBrand());
        log.info("totalPrice expected: {}, actual: {}", expected.getTotalPrice(priceBaseType), brand.getTotalPrice());
        Assertions.assertEquals(expected.getBrand(), brand.getBrand());
        Assertions.assertEquals(expected.getTotalPrice(priceBaseType), brand.getTotalPrice());

        var productMap = brand.getCategories().stream().collect(Collectors.toMap(CodiTargetPriceBrandResponse.PriceOfCategory::getCategory, product -> product));
        for (Map.Entry<String, Long> entry : expected.getPriceByCategory(priceBaseType).entrySet()) {
            String category = entry.getKey();
            long price = entry.getValue();
            Assertions.assertEquals(price, productMap.get(category).getPrice());
        }
    }

    private Pair<BrandSummary, BrandSummary> getBrandCategoryMinMax(int categoryCount) {
        // 각 브랜드별 합계 금액 계산하여 최저,최고가 브랜드 찾기
        var products = productService.getAllProducts();
        Map<String, BrandSummary> map = new LinkedHashMap<>();
        for (ProductDto.Product product : products) {
            BrandSummary summary = map.computeIfAbsent(product.getBrand(), _brand -> BrandSummary.builder().brand(product.getBrand()).build());
            summary.addProduct(product);
        }
        BrandSummary min = null;
        BrandSummary max = null;

        for (BrandSummary summary : map.values()) {
            if (summary.getCategoryCount() != categoryCount) {
                continue;
            }

            if (min == null || min.getTotalPrice(PriceType.PriceBaseType.Min) >= summary.getTotalPrice(PriceType.PriceBaseType.Min)) {
                min = summary;
            }

            if (max == null || max.getTotalPrice(PriceType.PriceBaseType.Max) <= summary.getTotalPrice(PriceType.PriceBaseType.Max)) {
                max = summary;
            }
        }

        return new ImmutablePair<>(min, max);
    }


    @Data
    @Builder
    public static class BrandSummary {
        private String brand;
        @Builder.Default
        private Map<String, Long> minPriceByCategory = new HashMap<>();
        @Builder.Default
        private Map<String, Long> maxPriceByCategory = new HashMap<>();

        public int getCategoryCount() {
            return minPriceByCategory.size();
        }

        public void addProduct(ProductDto.Product product) {
            Long minValue = minPriceByCategory.get(product.getCategory());
            if (minValue == null || minValue > product.getPrice()) {
                minPriceByCategory.put(product.getCategory(), product.getPrice());
            }
            Long maxValue = maxPriceByCategory.get(product.getCategory());
            if (maxValue == null || maxValue < product.getPrice()) {
                maxPriceByCategory.put(product.getCategory(), product.getPrice());
            }
        }

        public long getTotalPrice(PriceType.PriceBaseType priceBaseType) {
            if (priceBaseType == PriceType.PriceBaseType.Min) {
                return minPriceByCategory.values().stream().reduce(0L, Long::sum);
            } else {
                return maxPriceByCategory.values().stream().reduce(0L, Long::sum);
            }
        }

        public Map<String, Long> getPriceByCategory(PriceType.PriceBaseType priceBaseType) {
            if (priceBaseType == PriceType.PriceBaseType.Min) {
                return minPriceByCategory;
            } else {
                return maxPriceByCategory;
            }
        }
    }
}
