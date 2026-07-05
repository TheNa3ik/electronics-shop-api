package com.thena3ik.shopapi.dto.product;

import java.math.BigDecimal;

public record ProductPatchRequest(
        String name,
        String brand,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        Long categoryId) {
}
