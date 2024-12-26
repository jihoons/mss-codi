package com.mss.codi.service;

import com.mss.codi.controller.dto.CodiTargetPriceBrandResponse;
import com.mss.codi.controller.dto.CodiTargetPriceProductAllCategoryResponse;
import com.mss.codi.controller.dto.MinMaxProductByCategoryResponse;
import com.mss.codi.type.PriceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주요 API에서 사용할 내용 Caching
 * Parameter가 다양하지 않고 변경보다 조회가 훨씬 많아 Cache hitting rate 가 높을 거 같아서 주요 변경이 발생하면 cache 를 갱신함
 * 실제 서비스에서는 Redis나 Hazelcast와 같은 Cache server를 활용할 확률이 높을 것으로 보임
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CodiCacheService {
    private final CodiService codiService;

    private final Map<PriceType.PriceBaseType, CodiTargetPriceProductAllCategoryResponse> targetPriceByCategoryOfPriceType = new ConcurrentHashMap<>();
    private final Map<PriceType.PriceBaseType, CodiTargetPriceBrandResponse> targetPriceBrandOfPriceType = new ConcurrentHashMap<>();
    private final Map<String, MinMaxProductByCategoryResponse> minMaxProductByCategory = new ConcurrentHashMap<>();

    public void resetCache(Set<String> categories) {
        setTargetPriceByCategoryOfPriceType(PriceType.PriceBaseType.Min);
        setTargetPriceByCategoryOfPriceType(PriceType.PriceBaseType.Max);

        setTargetPriceByCategory(PriceType.PriceBaseType.Min);
        setTargetPriceByCategory(PriceType.PriceBaseType.Max);

        if (!CollectionUtils.isEmpty(categories)) {
            categories.forEach(this::setMinMaxProductByCategory);
        }
    }

    private void setTargetPriceByCategoryOfPriceType(PriceType.PriceBaseType priceBaseType) {
        targetPriceByCategoryOfPriceType.put(priceBaseType, codiService.getTargetPriceByCategory(priceBaseType));
    }

    public CodiTargetPriceProductAllCategoryResponse getTargetPriceByCategory(PriceType.PriceBaseType priceType) {
        return targetPriceByCategoryOfPriceType.get(priceType);
    }

    private void setTargetPriceByCategory(PriceType.PriceBaseType priceBaseType) {
        targetPriceBrandOfPriceType.put(priceBaseType, codiService.getTargetPriceBrand(priceBaseType));
    }

    public CodiTargetPriceBrandResponse getTargetPriceBrand(PriceType.PriceBaseType priceType) {
        return targetPriceBrandOfPriceType.get(priceType);
    }

    public void setMinMaxProductByCategory(String category) {
        minMaxProductByCategory.put(category, codiService.getMinMaxProductByCategory(category));
    }
    public MinMaxProductByCategoryResponse getMinMaxProductByCategory(String category) {
        return minMaxProductByCategory.get(category);
    }
}
