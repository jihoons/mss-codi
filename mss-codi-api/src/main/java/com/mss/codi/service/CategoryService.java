package com.mss.codi.service;

import com.mss.codi.entity.Category;
import com.mss.codi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrder();
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "카테고리(" + name + ")를 찾을 수 없습니다."));
    }
}
