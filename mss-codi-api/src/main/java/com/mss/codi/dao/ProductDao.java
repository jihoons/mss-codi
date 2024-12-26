package com.mss.codi.dao;

import com.mss.codi.entity.*;
import com.mss.codi.repository.ProductRepository;
import com.mss.codi.type.PriceType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ProductDao {
    private final JPAQueryFactory queryFactory;
    private final QProduct qProduct = QProduct.product;
    private final QBrand qBrand = QBrand.brand;
    private final QCategory qCategory = QCategory.category;
    private final ProductRepository productRepository;
    private final Environment environment;

    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        var query = queryFactory
                .select(qProduct)
                .from(qProduct)
                .innerJoin(qProduct.brand, qBrand) .fetchJoin()
                .innerJoin(qProduct.category, qCategory) .fetchJoin();
        if (StringUtils.isNotEmpty(category)) {
            query = query.where(qCategory.name.eq(category));
        }
        if (StringUtils.isNotEmpty(brand)) {
            query = query.where(qBrand.name.eq(brand));
        }
        return query.orderBy(qProduct.modifiedAt.asc())
                .fetch();
    }

    public List<Product> getAllProducts() {
        return queryFactory
                .select(qProduct)
                .from(qProduct)
                .innerJoin(qProduct.brand, qBrand) .fetchJoin()
                .innerJoin(qProduct.category, qCategory) .fetchJoin()
                .orderBy(qProduct.modifiedAt.asc())
                .fetch();
    }

    public Optional<Product> getProductById(long id) {
        return Optional.ofNullable(
                queryFactory
                .select(qProduct)
                .from(qProduct)
                .innerJoin(qProduct.brand, qBrand)
                .fetchJoin()
                .innerJoin(qProduct.category, qCategory)
                .fetchJoin()
                .where(qProduct.id.eq(id))
                .fetchOne()
        );
    }

    @Transactional
    public void clear() {
        if (StringUtils.equalsAny("test", environment.getActiveProfiles())) {
            productRepository.deleteProducts();
            queryFactory.delete(qBrand).execute();
            queryFactory.delete(qCategory).execute();
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getProductByCategoryAndBrand(
            Category category, Brand brand, PriceType priceType, long price, long maxProductCountOfBrand
    ) {
        var query = queryFactory.select(qProduct)
                .from(qProduct)
                .innerJoin(qProduct.brand, qBrand)
                .fetchJoin()
                .innerJoin(qProduct.category, qCategory)
                .fetchJoin()
                .where(qProduct.category.eq(category))
                .where(qProduct.brand.eq(brand));
        if (price > 0L) {
            query = query.where(qProduct.price.eq(price));
        }

        return query.orderBy(priceType.getBaseType().getOrderBy(), qProduct.modifiedAt.desc())
                .limit(maxProductCountOfBrand)
                .fetch();
    }
}
