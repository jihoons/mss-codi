package com.mss.codi.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    OnSale("onsale"),
    Deleted("deleted");

    private final String code;

    public static ProductStatus fromCode(String code) {
        // 기본값은 최저가
        if (StringUtils.isEmpty(code))
            return OnSale;

        return Arrays.stream(values()).filter(e -> e.getCode().equalsIgnoreCase(code)).findFirst().orElse(OnSale);
    }
}
