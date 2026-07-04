package com.thena3ik.shopapi.dto;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String brand,
        String description,
        Double price,
        Integer stockQuantity,
        Long categoryId,
        String categoryName) {
}
