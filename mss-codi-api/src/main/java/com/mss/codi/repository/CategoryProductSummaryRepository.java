package com.mss.codi.repository;

import com.mss.codi.entity.CategoryProductSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryProductSummaryRepository extends JpaRepository<CategoryProductSummary, Long> {
}
