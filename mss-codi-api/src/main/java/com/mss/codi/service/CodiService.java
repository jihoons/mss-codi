package com.mss.codi.service;

import com.mss.codi.controller.dto.CodiTargetPriceBrandResponse;
import com.mss.codi.controller.dto.CodiTargetPriceProductAllCategoryResponse;
import com.mss.codi.controller.dto.MinMaxProductByCategoryResponse;
import com.mss.codi.dao.CodiDao;
import com.mss.codi.dao.dto.TopBrandByCategory;
import com.mss.codi.entity.*;
import com.mss.codi.type.PriceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CodiService {
    private final CodiDao codiDao;
    private final CategoryService categoryService;

    @Value("${codi.category.brand.count:5}")
    private long maxBrandCountOfCategory;

    /**
     * 카테고리별최저/최고 가격브랜드와상품가격,총액을조회
     * @param priceBaseType 최저가/최고가 구분
     * @return
     */
    @Transactional(readOnly = true)
    public CodiTargetPriceProductAllCategoryResponse getTargetPriceByCategory(PriceType.PriceBaseType priceBaseType) {
        var products = codiDao.getCategoryPrice(priceBaseType);
        var response = CodiTargetPriceProductAllCategoryResponse.builder().build();

        products.stream()
                .map(this::convert2CategoryBrandDto)
                .forEach(response::addBrandOfCategory);
        return response;
    }

    private CodiTargetPriceProductAllCategoryResponse.BrandOfCategory convert2CategoryBrandDto(CategoryProductSummary summary) {
        return CodiTargetPriceProductAllCategoryResponse.BrandOfCategory.builder()
                .category(summary.getCategory().getName())
                .brand(summary.getProduct().getBrand().getName())
                .price(summary.getPrice())
                .build();
    }

    /**
     * 단일브랜드로 모든 카테고리 상품을 구매할 때 최저/최고가격에 판매하는 브랜드와 카테고리의 상품가격, 총액 조회
     * @param priceBaseType 최저가/최고가 구분
     * @return
     */
    @Transactional(readOnly = true)
    public CodiTargetPriceBrandResponse getTargetPriceBrand(PriceType.PriceBaseType priceBaseType) {
        var products = codiDao.getBrandCategoryPrice(priceBaseType);
        if (CollectionUtils.isEmpty(products)) {
            return CodiTargetPriceBrandResponse.builder().build();
        }

        var brand = CodiTargetPriceBrandResponse.Brand.builder()
                .brand(products.getFirst().getBrand().getName())
                .build();
        products.stream().map(this::convert2CategoryDto).forEach(brand::addProduct);

        if (priceBaseType == PriceType.PriceBaseType.Max) {
            return CodiTargetPriceBrandResponse.builder()
                    .maxBrand(brand)
                    .build();
        } else {
            return CodiTargetPriceBrandResponse.builder()
                    .minBrand(brand)
                    .build();
        }
    }

    private CodiTargetPriceBrandResponse.PriceOfCategory convert2CategoryDto(CategoryBrandProductSummary summary) {
        return CodiTargetPriceBrandResponse.PriceOfCategory.builder()
                .category(summary.getCategory().getName())
                .price(summary.getPrice())
                .build();
    }

    /**
     *
     * @param categoryName 카테고리 이름
     * @return
     */
    @Transactional(readOnly = true)
    public MinMaxProductByCategoryResponse getMinMaxProductByCategory(String categoryName) {
        if (StringUtils.isEmpty(categoryName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카테고리를 입력해주세요.");
        }

        Category category = categoryService.getCategoryByName(categoryName);
        var products = codiDao.getTopBrandCategoryPrice(category);
        var summaryByPriceType = products.stream().collect(Collectors.groupingBy(s -> s.getPriceType().getBaseType()));

        return MinMaxProductByCategoryResponse
                .builder()
                .category(category.getName())
                .minBrands(convert2BrandDto(summaryByPriceType.getOrDefault(PriceType.PriceBaseType.Min, Collections.emptyList())))
                .maxBrands(convert2BrandDto(summaryByPriceType.getOrDefault(PriceType.PriceBaseType.Max, Collections.emptyList())))
                .build();
    }

    private List<MinMaxProductByCategoryResponse.Brand> convert2BrandDto(List<TopBrandByCategory> summaries) {
        Set<String> brandSet = new HashSet<>();
        // 중복되는 brand는 제거
        return summaries.stream()
                .filter(s -> !brandSet.contains(s.getBrand()))
                .map(this::convert2BrandDto)
                .peek(p -> brandSet.add(p.getBrand()))
                .toList();
    }

    private MinMaxProductByCategoryResponse.Brand convert2BrandDto(TopBrandByCategory summary) {
        return MinMaxProductByCategoryResponse.Brand.builder()
                .brand(summary.getBrand())
                .price(summary.getPrice())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getCategoryProductSummaries(Category category, Set<PriceType> priceTypes) {
        return codiDao.getCategoryProductSummaries(category, priceTypes);
    }


    @Transactional
    public CategoryProductSummary saveCategoryProductSummary(CategoryProductSummary categoryProductSummary) {
        return codiDao.saveCategoryProductSummary(categoryProductSummary);
    }

    @Transactional
    public void removeCategoryProductSummary(CategoryProductSummary categoryProductSummary) {
        codiDao.removeCategoryProductSummary(categoryProductSummary);
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandProductSummaries(Category category, Brand brand) {
        return codiDao.getCategoryBrandProductSummaries(category, brand);
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandProductSummaries(Category category, Brand brand, PriceType priceType) {
        return codiDao.getCategoryBrandProductSummaries(category, brand, priceType);
    }

    @Transactional
    public CategoryBrandProductSummary saveCategoryBrandProductSummary(CategoryBrandProductSummary summary) {
        return codiDao.saveCategoryBrandProductSummary(summary);
    }

    @Transactional
    public void removeCategoryBrandProductSummary(CategoryBrandProductSummary categoryBrandProductSummary) {
        codiDao.removeCategoryBrandProductSummary(categoryBrandProductSummary);
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandSummaryByCategory(Category category, PriceType priceType) {
        return getCategoryBrandSummaryByCategory(category, priceType, 0L);
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandSummaryByCategory(Category category, PriceType priceType, long price) {
        List<CategoryBrandProductSummary> summaries = codiDao.getCategoryBrandSummaryByCategory(category, priceType, price, maxBrandCountOfCategory);
        if (CollectionUtils.isEmpty(summaries)) {
            return Collections.emptyList();
        }

        long targetPrice = summaries.getFirst().getPrice();
        return summaries.stream().filter(p -> p.getPrice() == targetPrice).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getCategoryBrandProductSummaryByProduct(long productId) {
        return codiDao.getCategoryBrandSummaryByProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<CategoryBrandProductSummary> getChangedCategoryBrandProductSummaryByProduct(Product product) {
        return codiDao.getChangedCategoryBrandSummaryByProduct(product);
    }

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getCategoryProductSummaryByProduct(long productId) {
        return codiDao.getCategorySummaryByProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<CategoryProductSummary> getChangedCategoryProductSummaryByProduct(Product product) {
        return codiDao.getChangedCategorySummaryByProduct(product);
    }
}
