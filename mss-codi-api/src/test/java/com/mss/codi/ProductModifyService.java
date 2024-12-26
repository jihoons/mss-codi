package com.mss.codi;

import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.service.ProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductModifyService {
    @Value("${codi.test.reset.waiting:3s}")
    private Duration resetWaiting;

    private final ProductService productService;

    public ProductModifyContext addProduct(List<ProductDto.NewProduct> products) {
        ProductModifyContext context = new ProductModifyContext();
        for (ProductDto.NewProduct product : products) {
            ProductDto.Product savedProduct = productService.addProduct(product);
            context.addProduct(savedProduct);
        }

        waitEventConsume();
        return context;
    }

    public void removeProduct(ProductModifyContext context) {
        for (ProductDto.Product product : context.getAddProducts()) {
            productService.removeProduct(product.getId());
        }
        waitEventConsume();
    }

    private void waitEventConsume() {
        try {
            TimeUnit.MILLISECONDS.sleep(resetWaiting.toMillis());
        } catch (InterruptedException ignored) {
        }
    }

    public void modifyProductPrice(ProductDto.Product product, long newPrice) {
        product.setPrice(newPrice);
        productService.saveProduct(product);
        waitEventConsume();
    }

    public void modifyProductPrice(String category, String brand, long price) {
        Optional<ProductDto.Product> first = findProduct(category, brand);
        first.ifPresent(product -> {
            product.setPrice(price);
            productService.saveProduct(product);
            waitEventConsume();
        });
    }

    public Optional<ProductDto.Product> findProduct(String category, String brand) {
        var products = productService.getAllProducts();
        return products.stream().filter(p -> StringUtils.equals(brand, p.getBrand()) && StringUtils.equals(category, p.getCategory())).findFirst();
    }

    public void modifyProductBrand(ProductDto.Product product, String newBrand) {
        product.setBrand(newBrand);
        productService.saveProduct(product);
        waitEventConsume();
    }

    public void modifyProductBrand(String category, String brand, String newBrand) {
        Optional<ProductDto.Product> first = findProduct(category, brand);
        first.ifPresent(product -> {
            product.setBrand(newBrand);
            productService.saveProduct(product);
            waitEventConsume();
        });
    }

    public void modifyProductCategory(ProductDto.Product product, String newCategory) {
        product.setCategory(newCategory);
        productService.saveProduct(product);
        waitEventConsume();
    }

    public void modifyProductCategory(String category, String brand, String newCategory) {
        Optional<ProductDto.Product> first = findProduct(category, brand);
        first.ifPresent(product -> {
            product.setCategory(newCategory);
            productService.saveProduct(product);
            waitEventConsume();
        });
    }

    @Data
    public static class ProductModifyContext {
        private List<ProductDto.Product> addProducts = new ArrayList<>();
        public void addProduct(ProductDto.Product product) {
            addProducts.add(product);
        }
    }
}
