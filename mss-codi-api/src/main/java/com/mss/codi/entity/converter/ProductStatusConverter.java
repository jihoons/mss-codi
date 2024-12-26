package com.mss.codi.entity.converter;

import com.mss.codi.type.ProductStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class ProductStatusConverter implements AttributeConverter<ProductStatus, String>  {
    @Override
    public String convertToDatabaseColumn(ProductStatus productStatus) {
        return productStatus.getCode();
    }

    @Override
    public ProductStatus convertToEntityAttribute(String s) {
        return ProductStatus.fromCode(s);
    }
}
