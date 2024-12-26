package com.mss.codi.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.NumberFormat;

public class PriceJsonSerializer extends JsonSerializer<Long> {
    private final NumberFormat formatter = NumberFormat.getNumberInstance();
    @Override
    public void serialize(Long price, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String formattedPrice = formatter.format(price);
        jsonGenerator.writeString(formattedPrice);
    }
}
