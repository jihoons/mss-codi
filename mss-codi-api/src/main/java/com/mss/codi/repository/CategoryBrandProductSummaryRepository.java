package com.mss.codi.repository;

import com.mss.codi.entity.CategoryBrandProductSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryBrandProductSummaryRepository extends JpaRepository<CategoryBrandProductSummary, Long> {
}
