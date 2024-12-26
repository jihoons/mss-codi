package com.mss.codi.controller;

import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public List<ProductDto.Product> getProducts(@RequestParam(required = false) String category, @RequestParam(required = false) String brand) {
        return productService.getProductsByCategoryBrand(category, brand);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto.Product addProduct(@Valid @RequestBody ProductDto.NewProduct product) {
        return productService.addProduct(product);
    }

    @PutMapping
    public ProductDto.Product modifyProduct(@Valid @RequestBody ProductDto.Product product) {
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public ProductDto.Product removeProduct(@Valid @Min(value = 1, message = "상품ID를 확인하세요.") @PathVariable long id) {
        return productService.removeProduct(id);
    }
}
