package com.mss.codi.controller;

import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="상품관리 API")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(description = "상품 조회")
    public List<ProductDto.Product> getProducts(
            @Schema(name = "카테고리") @RequestParam(required = false) String category,
            @Schema(name = "브랜드") @RequestParam(required = false) String brand) {
        return productService.getProductsByCategoryBrand(category, brand);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "상품 추가")
    public ProductDto.Product addProduct(@Valid @RequestBody ProductDto.NewProduct product) {
        return productService.addProduct(product);
    }

    @PutMapping
    @Operation(description = "상품 수정")
    public ProductDto.Product modifyProduct(@Valid @RequestBody ProductDto.Product product) {
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "상품 삭제")
    public ProductDto.Product removeProduct(@Valid @Min(value = 1, message = "상품ID를 확인하세요.") @PathVariable long id) {
        return productService.removeProduct(id);
    }
}
