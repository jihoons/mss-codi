package com.mss.codi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.entity.Product;
import com.mss.codi.exception.ErrorResponse;
import com.mss.codi.service.BrandService;
import com.mss.codi.service.ProductService;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

//@Slf4j
//@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestExecutionListeners({DataResetTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
//@ActiveProfiles("test")
public class ProductTest {
//    @Autowired
    private BrandService brandService;
//    @Autowired
    private ProductService productService;
//    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private RestClient restClient = RestClient.create();

//    @Test
    @DisplayName("브랜드 추가")
    public void testAddBrand() {
        var brand = brandService.addBrand("Z");
        var savedBrand = brandService.getBrandByName("Z");
        Assertions.assertNotNull(brand);
        Assertions.assertNotNull(savedBrand);
        Assertions.assertEquals(brand.getId(), savedBrand.getId());
    }

//    @Test
    @DisplayName("브랜드 중복")
    public void testBrandDuplication() {
        Assertions.assertThrows(ResponseStatusException.class, () -> {
            brandService.addBrand("A");
        });
    }

//    @Test
    @DisplayName("상품 추가")
    public void testProductAdd() {
        ProductDto.Product product = productService.addProduct(new ProductDto.NewProduct("A", "상의", 1000));
        Assertions.assertNotNull(product);
        Optional<Product> savedProduct = productService.getProduct(product.getId());
        Assertions.assertTrue(savedProduct.isPresent());
        Assertions.assertEquals(product.getId(), savedProduct.get().getId());
        Assertions.assertEquals(product.getBrand(), savedProduct.get().getBrand().getName());
        Assertions.assertEquals(product.getCategory(), savedProduct.get().getCategory().getName());
        Assertions.assertEquals(product.getPrice(), savedProduct.get().getPrice());
    }

//    @Test
    @DisplayName("상품입력오류")
    public void testProductAddError() {
        var response = callAddProduct(new ProductDto.NewProduct("", "상의", 1000));
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(StringUtils.isNotBlank(response.getReason()));

        response = callAddProduct(new ProductDto.NewProduct(null, "상의", 1000));
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(StringUtils.isNotBlank(response.getReason()));

        response = callAddProduct(new ProductDto.NewProduct("A", "", 1000));
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(StringUtils.isNotBlank(response.getReason()));

        response = callAddProduct(new ProductDto.NewProduct("A", null, 1000));
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(StringUtils.isNotBlank(response.getReason()));

        response = callAddProduct(new ProductDto.NewProduct("A", "상의", 0));
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(StringUtils.isNotBlank(response.getReason()));

        response = callAddProduct(new ProductDto.NewProduct("A", "상의", -1));
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(StringUtils.isNotBlank(response.getReason()));
    }

    private CallResponse callAddProduct(ProductDto.NewProduct newProduct) {
        return restClient.post()
                .uri("http://localhost:{port}/product", port)
                .body(newProduct)
                .exchange((request, response) -> {
                    var statusCode = response.getStatusCode();

                    if (statusCode.isError()) {
                        ErrorResponse errorResponse = objectMapper.readValue(response.getBody(), ErrorResponse.class);
                        return CallResponse.builder()
                                .success(false)
                                .reason(errorResponse.getMessage())
                                .build();
                    } else {
                        Product product = objectMapper.readValue(response.getBody(), Product.class);
                        return CallResponse.builder()
                                .success(true)
                                .product(product)
                                .build();
                    }
                });
    }

    @Data
    @Builder
    public static class CallResponse {
        private boolean success;
        private String reason;
        private Product product;
    }
}
