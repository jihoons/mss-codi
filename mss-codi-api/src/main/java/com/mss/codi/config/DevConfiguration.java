package com.mss.codi.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.entity.Brand;
import com.mss.codi.entity.Category;
import com.mss.codi.repository.CategoryRepository;
import com.mss.codi.service.BrandService;
import com.mss.codi.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile({"dev", "test"})
public class DevConfiguration {
    private final BrandService brandService;
    private final CategoryRepository categoryRepository;

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    private final Environment environment;

    @Value("${codi.test.reset.waiting:3s}")
    private Duration resetWaiting;

    // Test에서만 사용
    public void reset() throws Exception {
        productService.removeAll();
        dbInit();
        // event 처리가 될 시간을 만들어 줌
        try {
            TimeUnit.MILLISECONDS.sleep(resetWaiting.toMillis());
        } catch (InterruptedException ignored) {
        }
    }

    @PostConstruct
    public void init() throws Exception {
        if (StringUtils.equalsAny("test", environment.getActiveProfiles())) {
            return;
        }
        dbInit();
    }

    public void dbInit() throws Exception {
        var type = new TypeReference<List<CategoryData>>() { };
        var url  = ResourceUtils.getURL("classpath:sample/sample.json");
        var products = objectMapper.readValue(url, type);
        Map<String, Category> categoryMap = new LinkedHashMap<>();
        Map<String, Brand> brandMap = new LinkedHashMap<>();
        List<ProductDto.NewProduct> productList = new ArrayList<>();
        products.forEach(categoryData -> {
            var category = categoryMap.computeIfAbsent(categoryData.getCategory(), _name -> createCategory(_name, categoryMap.size()));
            categoryData.products.forEach(productData -> {
               var brand = brandMap.computeIfAbsent(productData.getBrand(), this::createBrand);
                productList.add(createProduct(category, brand, productData.getPrice()));
            });
        });
        categoryRepository.saveAll(categoryMap.values());

        productList.forEach(productService::addProduct);
    }

    private Category createCategory(String name, int size) {
        var category = new Category();
        category.setName(name);
        category.setDisplayOrder(size + 1);
        return category;
    }

    private Brand createBrand(String name) {
        return brandService.addBrand(name);
    }

    private ProductDto.NewProduct createProduct(Category category, Brand brand, long price) {
        var product = new ProductDto.NewProduct();
        product.setCategory(category.getName());
        product.setBrand(brand.getName());
        product.setPrice(price);
        return product;
    }

    @Data
    public static class CategoryData {
        private String category;
        private List<ProductData> products;
    }

    @Data
    public static class ProductData {
        private String brand;
        private long price;
    }
}
