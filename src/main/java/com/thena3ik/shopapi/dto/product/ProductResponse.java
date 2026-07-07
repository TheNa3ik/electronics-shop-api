package com.thena3ik.shopapi.dto.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String brand,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        Long categoryId,
        String categoryName) {
}
