package com.mss.codi.service.event;

import com.mss.codi.type.ProductChangeEventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductChangeEvent {
    private final ProductChangeEventType type;
    // 데이터 정합성을 위해서 상품 정보 자체보다는 ID만 전달하고 처리시에 최신 정보를 반영하도록 한다.
    private final long productId;
    // 데이터가 수정된 시간
    private final LocalDateTime lastModifiedAt;
}
