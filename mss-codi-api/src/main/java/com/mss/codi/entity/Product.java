package com.mss.codi.entity;

import com.mss.codi.config.ProductChangeListener;
import com.mss.codi.entity.converter.ProductStatusConverter;
import com.mss.codi.type.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "product", indexes = {
        @Index(name = "index_category_brand_price", columnList = "category, brand, price"),
        @Index(name = "index_modified_at", columnList = "modifiedAt")
})
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(ProductChangeListener.class)
@SQLDelete(sql = "update product set status = 'deleted', deleted_at = now() where id = ?")
@SQLRestriction("status = 'onsale'")
public class Product extends AuditTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brand")
    @NotNull
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category")
    @NotNull
    private Category category;

    private long price;

    @Convert(converter = ProductStatusConverter.class)
    private ProductStatus status = ProductStatus.OnSale;

    private LocalDateTime deletedAt;

    public Product(Category category, Brand brand, long price) {
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.status = ProductStatus.OnSale;
    }
}
