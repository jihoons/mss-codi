package com.mss.codi.service.event;

import com.mss.codi.entity.*;
import com.mss.codi.service.CodiCacheService;
import com.mss.codi.service.CodiService;
import com.mss.codi.service.ProductService;
import com.mss.codi.type.PriceType;
import com.mss.codi.type.ProductChangeEventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 상품 정보가 변경되었을 때 각 카테고리, 브랜드멸 최저가/최고가 상품 정보 갱신
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductDataSummarizer implements ProductChangeEventHandler {
    private final ProductService productService;
    private final CodiService codiService;
    private final CodiCacheService codiCacheService;
    private final ProductGlobalLockService productGlobalLockService;
    private final ProductChangeEventBroker productChangeEventBroker;

    @Override
    @Transactional
    public void handle(@NotNull ProductChangeEvent event) {
        // 영향받은 카테고리 목록
        Set<String> categories = new HashSet<>();
        // 삭제는 로직이 조금 다름
        if (event.getType() == ProductChangeEventType.Removed) {
            removeSummary(categories, event);
        } else {
            // 상품 정보 조회
            Optional<Product> product = productService.getProduct(event.getProductId());
            if (product.isEmpty()) {
                return;
            }

            // 브랜드나 카테고리가 변경되었으면 기존 요약본 삭제
            if (event.getType() == ProductChangeEventType.Modified) {
                checkCategoryBrandChanged(categories, event, product.get());
            }

            productGlobalLockService.doInLock(product.get().getCategory().getName(), () -> {
                // 카테고리/브랜드별 최저/최고 수정
                ModifyCategoryBrandResult result = modifyCategoryBrandProductSummary(categories, product.get());

                // 카테고리별 최저/최고 수정
                modifyCategoryProductSummary(categories, result, product.get());
            }, () -> {
                addFallback(event);
            });
        }

        if (!CollectionUtils.isEmpty(categories)) {
            // something is changed
            log.debug("changed category {}", categories);
            codiCacheService.resetCache(categories);
        }
    }

    public void addFallback(ProductChangeEvent event) {
        productChangeEventBroker.addChangedProduct(event);
    }

    // 최저가/최고가 삭제
    public void removeSummary(Set<String> categories, ProductChangeEvent event) {
        List<CategoryBrandProductSummary> summaries = codiService.getCategoryBrandProductSummaryByProduct(event.getProductId());
        if (CollectionUtils.isEmpty(summaries)) {
            return;
        }
        var first = summaries.getFirst();
        productGlobalLockService.doInLock(first.getCategory().getName(), () -> {
            if (resetCategoryBrandSummerByList(categories, summaries)) {
                removeCategoryProductSummary(categories, event.getProductId());
            }
        }, () -> {
            addFallback(event);
        });
    }

    private void checkCategoryBrandChanged(Set<String> categories, ProductChangeEvent event, Product product) {
        List<CategoryBrandProductSummary> summaries = codiService.getChangedCategoryBrandProductSummaryByProduct(product);
        if (CollectionUtils.isEmpty(summaries)) {
            return;
        }
        CategoryBrandProductSummary first = summaries.getFirst();
        // 과거 카테고리 변경
        productGlobalLockService.doInLock(first.getCategory().getName(), () -> {
            if (resetCategoryBrandSummerByList(categories, summaries)) {
                checkCategoryChanged(categories, product);
            }
        }, () -> {
            addFallback(event);
        });
    }

    private void checkCategoryChanged(Set<String> categories, Product product) {
        List<CategoryProductSummary> summaries = codiService.getChangedCategoryProductSummaryByProduct(product);
        resetCategorySummerByList(categories, summaries);
    }

    private void removeCategoryProductSummary(Set<String> categories, long productId) {
        List<CategoryProductSummary> summaries = codiService.getCategoryProductSummaryByProduct(productId);
        resetCategorySummerByList(categories, summaries);
    }

    private boolean resetCategoryBrandSummerByList(Set<String> categories, List<CategoryBrandProductSummary> summaries) {
        if (CollectionUtils.isEmpty(summaries)) {
            return false;
        }
        for (CategoryBrandProductSummary summary : summaries) {
            categories.add(summary.getCategory().getName());
            codiService.removeCategoryBrandProductSummary(summary);
            // 삭제된 카테고리, 브랜드의 남아있는 요약 정보 조회
            List<CategoryBrandProductSummary> summariesOfRemovedSummary = codiService.getCategoryBrandProductSummaries(summary.getCategory(), summary.getBrand(), summary.getPriceType());
            resetSummary(summariesOfRemovedSummary, summary, summary.getCategory(), summary.getBrand(), this::findCategoryBrandProductSummary);
        }

        return true;
    }

    private <T extends ProductSummary> void resetSummary(List<T> summariesOfRemovedSummary, T summary, Category category, Brand brand, TriConsumer<PriceType, Category, Brand> findTargetPrice) {
        if (CollectionUtils.isEmpty(summariesOfRemovedSummary)) {
            // 하나도 없으면 신규를 찾아야 함
            findTargetPrice.accept(summary.getPriceType(), category, brand);
        } else if (summary.getPriceType().isLast()) {
            // 만약 삭제한게 가장 최근 변경한 상품이라면 신규 최신을 찾아야 함
            Optional<T> first = summariesOfRemovedSummary.stream().min((o1, o2) -> o1.getModifiedAt().compareTo(o2.getModifiedAt()) * -1);
            first.ifPresent(s -> s.setPriceType(summary.getPriceType()));
        }
    }

    private void resetCategorySummerByList(Set<String> categories, List<CategoryProductSummary> summaries) {
        if (CollectionUtils.isEmpty(summaries)) {
            return;
        }

        for (CategoryProductSummary summary : summaries) {
            categories.add(summary.getCategory().getName());
            codiService.removeCategoryProductSummary(summary);

            // 삭제된 카테고리, 브랜드의 남아있는 요약 정보 조회
            List<CategoryProductSummary> summariesOfRemovedSummary = codiService.getCategoryProductSummaries(summary.getCategory(), PriceType.getFilterPriceTypes(summary.getPriceType()));
            resetSummary(summariesOfRemovedSummary, summary, summary.getCategory(), null, this::findCategoryProductSummary);
        }
    }

    // 카테고리/브랜드별 최저가/최고가 저장
    public ModifyCategoryBrandResult modifyCategoryBrandProductSummary(Set<String> categories, Product product) {
        List<CategoryBrandProductSummary> summaries = codiService.getCategoryBrandProductSummaries(product.getCategory(), product.getBrand());

        if (CollectionUtils.isEmpty(summaries)) {
            saveCategoryBrandProductSummary(PriceType.LastMin, product);
            saveCategoryBrandProductSummary(PriceType.LastMax, product);
            return new ModifyCategoryBrandResult(true, true);
        }

        var summaryMap = summaries.stream().collect(Collectors.groupingBy(s -> s.getPriceType().getBaseType()));
        ModifyCategoryBrandResult result = new ModifyCategoryBrandResult(false, false);
        summaryMap.forEach((priceType, list) -> {
            var changed = changeSummaries(
                    categories,
                    product,
                    list,
                    PriceType.getTargetPriceType(priceType),
                    PriceType.getLastPriceType(priceType),
                    this::saveCategoryBrandProductSummary,
                    this::findCategoryBrandProductSummary,
                    codiService::removeCategoryBrandProductSummary,
                    CategoryBrandProductSummary.class
            );

            if (priceType == PriceType.PriceBaseType.Min) {
                result.setMinChanged(changed);
            } else {
                result.setMaxChanged(changed);
            }
        });

        return result;
    }

    public void modifyCategoryProductSummary(Set<String> categories, ModifyCategoryBrandResult brandModifyResult, Product product) {
        // 브랜드에서 수정이 되지 않았으면 카테고리는 굳이 변경을 시도할 필요 없음
        if (brandModifyResult.isNoChanged()) {
            return;
        }

        var summaries = codiService.getCategoryProductSummaries(product.getCategory(), brandModifyResult.getNeedPriceTypes());
        if (CollectionUtils.isEmpty(summaries)) {
            // 신규로 생성
            if (brandModifyResult.isMinChanged())
                saveCategoryProductSummary(PriceType.LastMin, product);

            if (brandModifyResult.isMaxChanged())
                saveCategoryProductSummary(PriceType.LastMax, product);
            return;
        }

        // 유형별로 구분하여 목록화
        var listByPriceType = summaries.stream().collect(Collectors.groupingBy(s -> s.getPriceType().getBaseType()));
        listByPriceType.forEach((priceType, list) -> {
            changeSummaries(
                    categories, product, list,
                    PriceType.getTargetPriceType(priceType),
                    PriceType.getLastPriceType(priceType),
                    this::saveCategoryProductSummary,
                    this::findCategoryProductSummary,
                    codiService::removeCategoryProductSummary,
                    CategoryProductSummary.class
            );
        });
    }

    private <T extends ProductSummary> boolean changeSummaries(
            Set<String> categories,
            Product product,
            List<T> summaries,
            PriceType targetType, PriceType lastType,
            BiConsumer<PriceType, Product> saver,
            TriConsumer<PriceType, Category, Brand> findTargetPrice,
            Consumer<T> remover,
            Class<T> clazz) {
        // 기존 최저가나 최고가로 등록되어 있는 상품이 변경된 경우 별도 처리가 필요
        T sameProductSummary = null;
        var changeResult = ChangeResult.NoEffect;
        for (T summary : summaries) {
            if (sameProductSummary == null && Objects.equals(summary.getProductId(), product.getId())) {
                sameProductSummary = summary;
            }
            changeResult = changeSavedSummary(product, summary, targetType, (product1, summary1) -> {
                int compareValue = compareProductPrice(product1, summary1);
                // compareProductPrice 함수는 최저가를 대상으로 구현했기 때문에 최고가인 경우 반대로 수정
                if (targetType == PriceType.Min) {
                    return compareValue;
                } else {
                    return compareValue * -1;
                }
            }, remover, clazz);
        }

        if (sameProductSummary != null) {
            if (changeResult == ChangeResult.NoEffect) {
                // 기존 최저가/최고가 상품 가격이 변경되어 최저가나 최고가가 아닌 경우 삭제 처리
                // 예를 들어 최저가였던 1000원짜리 상품을 1100원으로 변경시 기존에 최저가에 영향을 주진 않지만
                // 더 이상 최저가임을 보장할 수 없으니 추가 확인 필요
                remover.accept(sameProductSummary);
                resetMinMaxProduct(categories, product.getCategory(), product.getBrand(), summaries, sameProductSummary, targetType, lastType, findTargetPrice);
            } else if (changeResult == ChangeResult.Removed) {
                resetMinMaxProduct(categories, product.getCategory(), product.getBrand(), summaries, sameProductSummary, targetType, lastType, findTargetPrice);
            }
            return true;
        } else if (changeResult != ChangeResult.NoEffect) {
            saver.accept(lastType, product);
            categories.add(product.getCategory().getName());
            return true;
        }
        return false;
    }

    private <T extends ProductSummary> void resetMinMaxProduct(
            Set<String> categories,
            Category category, Brand brand,
            List<T> summaries, T sameProductSummary,
            PriceType targetType, PriceType lastType,
            TriConsumer<PriceType, Category, Brand> findTargetPrice) {
        categories.add(category.getName());
        // 만약 최고가나 최저가 상품이 하나만 있었다면 유일하게 변경된 상품만 있었으면 다른 상품을 찾아서 저장
        // 두개 이상이면 나머지중 가장 최근에 변경된 상품을 Last로 설정
        if (summaries.size() == 1) {
            findTargetPrice.accept(targetType, category, brand);
        } else {
            Optional<T> first = summaries.stream().filter(s -> s != sameProductSummary).min((o1, o2) -> o1.getModifiedAt().compareTo(o2.getModifiedAt()) * -1);
            first.ifPresent(summary -> summary.setPriceType(lastType));
        }
    }

    private int compareProductPrice(Product product, ProductSummary summary) {
        return Long.compare(product.getPrice(), summary.getPrice());
    }

    public enum ChangeResult {
        // 기존 저장 내역 삭제
        Removed,
        // 기존 최저/최고 가격과 같음
        Same,
        // 아무 영향 없음
        NoEffect;
    }

    private <T extends ProductSummary> ChangeResult changeSavedSummary(Product product, T summary, PriceType targetType, BiFunction<Product, T, Integer> compare, Consumer<T> remover, Class<T> clazz) {
        int compareValue = compare.apply(product, summary);
        // 변경 상품이 영향이 없는 경우
        if (compareValue > 0) {
            return ChangeResult.NoEffect;
        }

        if (compareValue == 0) {
            // 변경 상품이 최저가거나 최고가인 경우 기존 Last를 기본값으로 변경
            // 단, 같은 상품이면 변경하지 않음
            if (summary.getPriceType().isLast() && !Objects.equals(summary.getProductId(), product.getId())) {
                summary.setPriceType(targetType);
            }
            return ChangeResult.Same;
        }

        // 변경 상품 보다 기존 가격보다 더 적거나 높으면 기존 상품 제외
        remover.accept(summary);
        return ChangeResult.Removed;
    }

    private void saveCategoryProductSummary(PriceType priceType, Product product) {
        var summary = createCategorySummary(priceType, product);
        codiService.saveCategoryProductSummary(summary);
    }

    private void findCategoryProductSummary(PriceType priceType, Category category, Brand brand) {
        List<CategoryBrandProductSummary> summaries = codiService.getCategoryBrandSummaryByCategory(category, priceType);
        if (CollectionUtils.isEmpty(summaries)) {
            return;
        }

        // price 0 원이면 첫번째는 Last로 저장
        AtomicBoolean isFirst = new AtomicBoolean(true);
        summaries.forEach(summary -> {
            if (isFirst.get()) {
                saveCategoryProductSummary(PriceType.getLastPriceType(priceType.getBaseType()), summary.getProduct());
                isFirst.set(false);
            } else {
                saveCategoryProductSummary(priceType, summary.getProduct());
            }
        });
    }

    private CategoryProductSummary createCategorySummary(PriceType priceType, Product product) {
        CategoryProductSummary summary = new CategoryProductSummary();
        summary.setCategory(product.getCategory());
        summary.setProduct(product);
        summary.setPriceType(priceType);
        summary.setPrice(product.getPrice());
        summary.setModifiedAt(product.getModifiedAt());
        summary.setCreatedAt(product.getModifiedAt());

        return summary;
    }

    private void saveCategoryBrandProductSummary(PriceType priceType, Product product) {
        var summary = createCategoryBrandSummary(priceType, product);
        codiService.saveCategoryBrandProductSummary(summary);
    }

    private void findCategoryBrandProductSummary(PriceType priceType, Category category, Brand brand) {
        List<Product> products = productService.getTopPriceProductByCategoryAndBrand(category, brand, priceType);
        if (CollectionUtils.isEmpty(products)) {
            return;
        }

        // price 0 원이면 첫번째는 Last로 저장
        AtomicBoolean isFirst = new AtomicBoolean(true);
        products.forEach(product1 -> {
            if (isFirst.get()) {
                saveCategoryBrandProductSummary(PriceType.getLastPriceType(priceType.getBaseType()), product1);
                isFirst.set(false);
            } else {
                saveCategoryBrandProductSummary(priceType, product1);
            }
        });
    }

    private CategoryBrandProductSummary createCategoryBrandSummary(PriceType priceType, Product product) {
        CategoryBrandProductSummary summary = new CategoryBrandProductSummary();
        summary.setCategory(product.getCategory());
        summary.setBrand(product.getBrand());
        summary.setProduct(product);
        summary.setPriceType(priceType);
        summary.setPrice(product.getPrice());
        summary.setModifiedAt(product.getModifiedAt());
        summary.setCreatedAt(product.getModifiedAt());

        return summary;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ModifyCategoryBrandResult {
        private boolean minChanged;
        private boolean maxChanged;

        public boolean isNoChanged() {
            return !minChanged && !maxChanged;
        }

        public boolean isChangedAll() {
            return minChanged && maxChanged;
        }

        public Set<PriceType> getNeedPriceTypes() {
            if (isChangedAll()) {
                return Set.of(PriceType.Min, PriceType.LastMin, PriceType.Max, PriceType.LastMax);
            } else if (maxChanged) {
                return Set.of(PriceType.Max, PriceType.LastMax);
            } else if (minChanged) {
                return Set.of(PriceType.Min, PriceType.LastMin);
            } else {
                return Collections.emptySet();
            }
        }
    }
}
