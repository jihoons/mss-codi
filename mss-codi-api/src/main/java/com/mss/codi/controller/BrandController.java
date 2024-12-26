package com.mss.codi.controller;

import com.mss.codi.controller.dto.BrandDto;
import com.mss.codi.entity.Brand;
import com.mss.codi.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/brand")
@Tag(name="브랜드 관리 API")
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    @Operation(description = "전체 브랜드 조회")
    public List<Brand> getAllBrands() {
        return brandService.getAllBrands();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "브랜드 신규 생성")
    public Brand createBrand(@RequestBody BrandDto.NewBrand brand) {
        return brandService.addBrand(brand.getName());
    }
}
