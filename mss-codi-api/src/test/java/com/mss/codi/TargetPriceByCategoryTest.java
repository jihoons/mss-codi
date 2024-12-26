package com.mss.codi;

import com.mss.codi.controller.dto.CodiTargetPriceProductAllCategoryResponse;
import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.entity.Category;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(classes = Application.class)
@TestExecutionListeners({DataResetTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@ActiveProfiles(value = "test")
public class TargetPriceByCategoryTest {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CodiService codiService;
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductModifyService productModifyService;

    @Test
    @DisplayName("카테고리별 최저/최고가격 브랜드 검증")
    public void testCategoryMinMaxBrand() {
        var categories = categoryService.getAllCategories();
        testCategoryMinMax(categories);
    }

    private Pair<Map<String, Brand>, Map<String, Brand>> testCategoryMinMax(List<Category> categories) {
        Pair<Map<String, Brand>, Map<String, Brand>> productsMinMax = getProductsMinMax();
        Map<String, Brand> minMap = productsMinMax.getLeft();
        Map<String, Brand> maxMap = productsMinMax.getRight();
        testCategoryMinMax(minMap, categories, PriceType.PriceBaseType.Min);
        testCategoryMinMax(maxMap, categories, PriceType.PriceBaseType.Max);
        return productsMinMax;
    }

    @Test
    @DisplayName("상품변경 후 카테고리별 최저/최고가격 브랜드 검증")
    public void testModifyProductCategoryMaxMaxBrand() {
        var categories = categoryService.getAllCategories();

        var newProduct = new ProductDto.NewProduct("A", "상의", 10L);
        var newMaxProduct = new ProductDto.NewProduct("A", "상의", 200000L);
        var productModifyContext = productModifyService.addProduct(List.of(newProduct, newMaxProduct));


        // 추가 상품 삭제
        productModifyService.removeProduct(productModifyContext);

        // 삭제 후 값 검증
        testCategoryMinMax(categories);

        Optional<ProductDto.Product> optProduct = productModifyService.findProduct("상의", "C");
        Assertions.assertTrue(optProduct.isPresent());
        var product = optProduct.get();

        // 상품 가격 변경
        productModifyService.modifyProductPrice(product, 200000L);
        testCategoryMinMax(categories);

        productModifyService.modifyProductCategory(product, "양말");
        testCategoryMinMax(categories);

        productModifyService.modifyProductBrand(product, "D");
        testCategoryMinMax(categories);
    }

    private void testCategoryMinMax(Map<String, Brand> expectMap, List<Category> categories, PriceType.PriceBaseType priceBaseType) {
        var response = codiService.getTargetPriceByCategory(priceBaseType);
        log.info("카테고리별 {} 브랜드 조회\n{}", priceBaseType, response);
        Map<String, CodiTargetPriceProductAllCategoryResponse.BrandOfCategory> productMap = response.getTargetBrandOfCategories().stream().collect(Collectors.toMap(CodiTargetPriceProductAllCategoryResponse.BrandOfCategory::getCategory, product -> product));
        Assertions.assertNotNull(productMap);

        // 모든 카테고리가 다 있어야 한다.
        Assertions.assertEquals(categories.size(), response.getTargetBrandOfCategories().size());
        long totalPrice = 0L;
        for (int i = 0; i < categories.size(); i++) {
            var category = categories.get(i);
            var product = response.getTargetBrandOfCategories().get(i);
            // 카테고리 순서와 일치해야 한다.
            Assertions.assertEquals(category.getName(), product.getCategory());
            assertProduct(productMap, expectMap, category.getName());
            totalPrice = totalPrice + product.getPrice();
        }

        Assertions.assertEquals(totalPrice, response.getTotalPrice());
    }

    private void assertProduct(Map<String, CodiTargetPriceProductAllCategoryResponse.BrandOfCategory> productMap, Map<String, Brand> expectMap, String category) {
        var product = productMap.get(category);
        var expectBrand = expectMap.get(category);
        String brand = expectBrand.getBrand();
        long price = expectBrand.getPrice();
        Assertions.assertNotNull(product);
        Assertions.assertEquals(brand, product.getBrand());
        Assertions.assertEquals(price, product.getPrice());

    }

    private Pair<Map<String, Brand>, Map<String, Brand>> getProductsMinMax() {
        Map<String, Brand> minMap = new HashMap<>();
        Map<String, Brand> maxMap = new HashMap<>();
        List<ProductDto.Product> products = productService.getAllProducts();
        for (ProductDto.Product product : products) {
            // 같은 가격이면 최근 수정된 상품을 노출
            addBrand(minMap, product, (product1, brand) -> product1.getPrice() <= brand.getPrice());
            addBrand(maxMap, product, (product1, brand) -> product1.getPrice() >= brand.getPrice());
        }

        return new ImmutablePair<>(minMap, maxMap);
    }

    private void addBrand(Map<String, Brand> map, ProductDto.Product product, BiFunction<ProductDto.Product, Brand, Boolean> compare) {
        // 카테고리 확인
        Brand brand = map.get(product.getCategory());
        if (brand == null) {
            brand = Brand.builder()
                    .category(product.getCategory())
                    .brand(product.getBrand())
                    .price(product.getPrice())
                    .build();
            map.put(product.getCategory(), brand);
        } else if (compare.apply(product, brand)) {
            brand.setPrice(product.getPrice());
            brand.setBrand(product.getBrand());
        }
    }

    @Data
    @Builder
    public static class Brand {
        private String category;
        private String brand;
        private long price;
    }

}
