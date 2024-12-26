package com.mss.codi.controller;

import com.mss.codi.controller.dto.BrandDto;
import com.mss.codi.entity.Brand;
import com.mss.codi.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/brand")
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public List<Brand> getAllBrands() {
        return brandService.getAllBrands();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Brand createBrand(@RequestBody BrandDto.NewBrand brand) {
        return brandService.addBrand(brand.getName());
    }
}
