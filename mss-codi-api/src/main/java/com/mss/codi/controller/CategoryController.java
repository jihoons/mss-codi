package com.mss.codi.controller;

import com.mss.codi.entity.Category;
import com.mss.codi.repository.CategoryRepository;
import com.mss.codi.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
@Tag(name = "카테고리 API")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(description = "전체 카테고리 조회")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
