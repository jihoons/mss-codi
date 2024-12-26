package com.mss.codi.dao;

import com.mss.codi.dao.dto.TopBrandByCategory;
import com.mss.codi.entity.*;
import com.mss.codi.repository.CategoryBrandProductSummaryRepository;
import com.mss.codi.repository.CategoryProductSummaryRepository;
import com.mss.codi.type.PriceType;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CodiDao {
    private final JPAQueryFactory queryFactory;
    private final QCategoryProductSummary qCategoryProductSummary = QCategoryProductSummary.categoryProductSummary;
    private final QCategoryBrandProductSummary qCategoryBrandProductSummary = QCategoryBrandProductSummary.categoryBrandProductSummary;
    private final QProduct qProduct = QProduct.product;
    private final QBrand qBrand = QBrand.brand;
    private final QCategory qCategory = QCategory.category;
    private final CategoryProductSummaryRepository categoryProductSummaryRepository;
    private final CategoryBrandProductSummaryRepository categoryBrandProductSummaryRepository;

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getCategoryPrice(PriceType.PriceBaseType priceBaseType) {
        return queryFactory
                .select(qCategoryProductSummary)
                .from(qCategoryProductSummary)
                .innerJoin(qCategoryProductSummary.product, qProduct).fetchJoin()
                .innerJoin(qCategoryProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qProduct.brand, qBrand).fetchJoin()
                .where(qCategoryProductSummary.priceType.eq(PriceType.getLastPriceType(priceBaseType)))
                .orderBy(qCategory.displayOrder.asc())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getBrandCategoryPrice(PriceType.PriceBaseType priceBaseType) {
        var lastType = PriceType.getLastPriceType(priceBaseType);

        var brand = queryFactory.select(qCategoryBrandProductSummary.brand)
                .from(qCategoryBrandProductSummary)
                .where(qCategoryBrandProductSummary.priceType.eq(lastType))
                .groupBy(qCategoryBrandProductSummary.brand)
                // 모든 카테고리 상품이 있어야 됨
                .having(qCategoryBrandProductSummary.category.countDistinct().eq(JPAExpressions.select(qCategory.count()).from(qCategory)))
                .orderBy(priceBaseType == PriceType.PriceBaseType.Max ? qCategoryBrandProductSummary.price.sum().desc() : qCategoryBrandProductSummary.price.sum().asc())
                .limit(1L)
                .fetchOne();

        if (brand == null) {
            return Collections.emptyList();
        }

        return queryFactory.select(qCategoryBrandProductSummary)
                .from(qCategoryBrandProductSummary)
                .innerJoin(qCategoryBrandProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.brand, qBrand).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.product, qProduct).fetchJoin()
                .where(qCategoryBrandProductSummary.priceType.eq(lastType))
                .where(qCategoryBrandProductSummary.brand.eq(brand))
                .orderBy(qCategory.displayOrder.asc())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<TopBrandByCategory> getTopBrandCategoryPrice(Category category) {
        return queryFactory.selectDistinct(qCategoryProductSummary.priceType,
                        qCategoryProductSummary.product.brand.name,
                        qCategoryProductSummary.product.price,
                        qCategory.displayOrder)
                .from(qCategoryProductSummary)
                .innerJoin(qCategoryProductSummary.product, qProduct)
                .innerJoin(qProduct.brand, qBrand)
                .innerJoin(qCategoryProductSummary.category, qCategory)
                .where(qCategory.eq(category))
                .orderBy(qCategory.displayOrder.asc())
                .fetch()
                .stream()
                .map(tuple ->
                    TopBrandByCategory.builder()
                            .brand(tuple.get(qCategoryProductSummary.product.brand.name))
                            .price(Objects.requireNonNullElse(tuple.get(qCategoryProductSummary.product.price), 0L))
                            .priceType(tuple.get(qCategoryProductSummary.priceType))
                            .build()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getCategoryProductSummaries(Category category, Set<PriceType> priceTypes) {
        return queryFactory.select(qCategoryProductSummary)
                .from(qCategoryProductSummary)
                .innerJoin(qCategoryProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qCategoryProductSummary.product, qProduct).fetchJoin()
                .innerJoin(qCategoryProductSummary.product.brand, qBrand).fetchJoin()
                .where(qCategoryProductSummary.category.eq(category))
                .where(qCategoryProductSummary.priceType.in(priceTypes))
                .fetch();
    }

    @Transactional
    public CategoryProductSummary saveCategoryProductSummary(CategoryProductSummary summary) {
        return categoryProductSummaryRepository.saveAndFlush(summary);
    }

    @Transactional
    public void removeCategoryProductSummary(CategoryProductSummary summary) {
        categoryProductSummaryRepository.delete(summary);
        categoryProductSummaryRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandProductSummaries(Category category, Brand brand) {
        return getCategoryBrandProductSummaries(category, brand, PriceType.None);
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandProductSummaries(Category category, Brand brand, PriceType priceType) {
        var query = queryFactory.select(qCategoryBrandProductSummary)
                .from(qCategoryBrandProductSummary)
                .innerJoin(qCategoryBrandProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.brand, qBrand).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.product, qProduct).fetchJoin()
                .where(qCategoryBrandProductSummary.category.eq(category))
                .where(qCategoryBrandProductSummary.brand.eq(brand));
        if (priceType != PriceType.None) {
            query = query.where(qCategoryBrandProductSummary.priceType.in(PriceType.getFilterPriceTypes(priceType)));
        }
        return query.fetch();
    }

    @Transactional
    public CategoryBrandProductSummary saveCategoryBrandProductSummary(CategoryBrandProductSummary summary) {
        return categoryBrandProductSummaryRepository.saveAndFlush(summary);
    }

    @Transactional
    public void removeCategoryBrandProductSummary(CategoryBrandProductSummary summary) {
        categoryBrandProductSummaryRepository.delete(summary);
        categoryBrandProductSummaryRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandSummaryByCategory(
            Category category, PriceType priceType, long price, long maxBrandCountOfCategory
    ) {
        var query = queryFactory
                .select(qCategoryBrandProductSummary)
                .from(qCategoryBrandProductSummary)
                .innerJoin(qCategoryBrandProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.brand, qBrand).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.product, qProduct).fetchJoin()
                .where(qCategoryBrandProductSummary.category.eq(category))
                .where(qCategoryBrandProductSummary.priceType.in(PriceType.getFilterPriceTypes(priceType)));

        if (price > 0) {
            query = query.where(qCategoryBrandProductSummary.product.price.eq(price));
        }
        return query.orderBy(priceType.getBaseType().getOrderBy(), qCategoryBrandProductSummary.modifiedAt.desc())
                .limit(maxBrandCountOfCategory)
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getCategorySummaryByProduct(long productId) {
        return queryFactory
                .select(qCategoryProductSummary)
                .from(qCategoryProductSummary)
                .innerJoin(qCategoryProductSummary.category, qCategory).fetchJoin()
                .where(qCategoryProductSummary.product.id.eq(productId))
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getChangedCategorySummaryByProduct(Product product) {
        return queryFactory
                .select(qCategoryProductSummary)
                .from(qCategoryProductSummary)
                .innerJoin(qCategoryProductSummary.category, qCategory).fetchJoin()
                .where(qCategoryProductSummary.product.eq(product))
                .where(qCategoryProductSummary.category.ne(product.getCategory()))
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandSummaryByProduct(long productId) {
        return queryFactory
                .select(qCategoryBrandProductSummary)
                .from(qCategoryBrandProductSummary)
                .innerJoin(qCategoryBrandProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.brand, qBrand).fetchJoin()
                .where(qCategoryBrandProductSummary.product.id.eq(productId))
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getChangedCategoryBrandSummaryByProduct(Product product) {
        return queryFactory
                .select(qCategoryBrandProductSummary)
                .from(qCategoryBrandProductSummary)
                .innerJoin(qCategoryBrandProductSummary.category, qCategory).fetchJoin()
                .innerJoin(qCategoryBrandProductSummary.brand, qBrand).fetchJoin()
                .where(qCategoryBrandProductSummary.product.eq(product))
                .where(qCategoryBrandProductSummary.category.ne(product.getCategory()).or(qCategoryBrandProductSummary.brand.ne(product.getBrand())))
                .fetch();
    }

    @Transactional
    public void truncateSummaries() {
        queryFactory.delete(qCategoryProductSummary).execute();
        queryFactory.delete(qCategoryBrandProductSummary).execute();
    }
}
