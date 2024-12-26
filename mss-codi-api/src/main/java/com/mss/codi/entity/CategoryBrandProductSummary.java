package com.mss.codi.entity;

import com.mss.codi.entity.converter.PriceTypeConverter;
import com.mss.codi.type.PriceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "category_brand_product_summary", indexes = {@Index(name = "category_brand_product_summary_product", columnList = "product")})
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBrandProductSummary implements ProductSummary{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand", nullable = false)
    private Brand brand;

    // 최저가, 최고가 등 상태 저장
    @Column(name = "priceType")
    @Convert(converter = PriceTypeConverter.class)
    private PriceType priceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product")
    private Product product;

    // 최저가/최고가로 저장된 시점의 가격
    private long price;

    // 상품 변경시간과 일치시키기 위해 의도적으로 Audit을 사용하지 않음
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @Override
    public Long getProductId() {
        return product.getId();
    }
}
