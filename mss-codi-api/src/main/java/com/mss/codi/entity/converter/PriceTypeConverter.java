package com.mss.codi.entity.converter;

import com.mss.codi.type.PriceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class PriceTypeConverter implements AttributeConverter<PriceType, String> {
    @Override
    public String convertToDatabaseColumn(PriceType attribute) {
        return attribute.getCode();
    }

    @Override
    public PriceType convertToEntityAttribute(String dbData) {
        return PriceType.fromCode(dbData);
    }
}
