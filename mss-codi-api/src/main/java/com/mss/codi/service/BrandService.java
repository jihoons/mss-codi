package com.mss.codi.service;

import com.mss.codi.entity.Brand;
import com.mss.codi.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrandService {
    private final BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional
    public Brand addBrand(String name) {
        Optional<Brand> optionalBrand = brandRepository.findByName(name);
        if (optionalBrand.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 등록된 브랜드(" + name + ") 입니다.");
        }
        Brand brand = new Brand();
        brand.setName(name);
        return brandRepository.save(brand);
    }

    public Brand getBrandByName(String name) {
        return brandRepository.findByName(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "브랜드(" + name + ")를 찾을 수 없습니다."));
    }
}
