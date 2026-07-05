package com.thena3ik.shopapi.dto.product;

import java.math.BigDecimal;

public record ProductFilterRequest(
        String brand,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        String search) {
}
