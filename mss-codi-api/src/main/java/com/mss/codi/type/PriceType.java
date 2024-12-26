package com.mss.codi.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mss.codi.entity.QProduct;
import com.querydsl.core.types.OrderSpecifier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum PriceType {
    // 최저가
    Min("min", PriceBaseType.Min, false),
    // 최고가
    Max("max", PriceBaseType.Max, false),
    // 최저가면서 가장 최근 변경 상품
    LastMin("lastest_min", PriceBaseType.Min, true),
    // 최고가면서 가장 최근 변경 상품
    LastMax("lastest_max", PriceBaseType.Max, true),
    None("none", PriceBaseType.None, false);

    private final String code;
    private final PriceBaseType baseType;
    private final boolean last;

    @JsonCreator
    public static PriceType fromCode(String code) {
        // 기본값은 최저가
        if (StringUtils.isEmpty(code))
            return Min;

        return Arrays.stream(values()).filter(e -> e.getCode().equalsIgnoreCase(code)).findFirst().orElse(Min);
    }

    public static Set<PriceType> getFilterPriceTypes(PriceType priceType) {
        if (priceType.getBaseType() == PriceBaseType.Min) {
            return Set.of(Min, LastMin);
        } else if (priceType.getBaseType() == PriceBaseType.Max) {
            return Set.of(Max, LastMax);
        } else {
            return Collections.emptySet();
        }
    }

    public static PriceType getTargetPriceType(PriceBaseType baseType) {
        if (baseType == PriceBaseType.Max) {
            return Max;
        } else {
            return Min;
        }
    }

    public static PriceType getLastPriceType(PriceBaseType baseType) {
        if (baseType == PriceBaseType.Max) {
            return LastMax;
        } else {
            return LastMin;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum PriceBaseType {
        @Schema(description = "최저가")
        Min("min", QProduct.product.price.asc()),
        @Schema(description = "최고가")
        Max("max", QProduct.product.price.desc()),
        None("", null);

        @JsonValue
        private final String code;
        private final OrderSpecifier<Long> orderBy;

        @JsonCreator
        public static PriceBaseType fromCode(String code) {
            // 기본값은 최저가
            if (StringUtils.isEmpty(code))
                return Min;

            return Arrays.stream(values()).filter(e -> e.getCode().equalsIgnoreCase(code)).findFirst().orElse(Min);
        }
    }

    @Component
    public static class StringToPriceBaseTypeConverter implements Converter<String, PriceBaseType> {
        @Override
        public PriceBaseType convert(String source) {
            return PriceBaseType.fromCode(source);
        }
    }
}
