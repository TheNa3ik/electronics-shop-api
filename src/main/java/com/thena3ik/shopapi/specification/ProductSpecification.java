package com.thena3ik.shopapi.specification;

import com.thena3ik.shopapi.entity.Product;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static PredicateSpecification<Product> hasBrand(String brand) {
        return (from, builder) -> builder.equal(from.get("brand"), brand);
    }

    public static PredicateSpecification<Product> hasCategory(Long categoryId) {
        return (from, builder) -> builder.equal(from.get("category").get("id"), categoryId);
    }

    public static PredicateSpecification<Product> hasPriceMin(BigDecimal minPrice) {
        return (from, builder) -> builder.greaterThanOrEqualTo(from.get("price"), minPrice);
    }

    public static PredicateSpecification<Product> hasPriceMax(BigDecimal maxPrice) {
        return (from, builder) -> builder.lessThanOrEqualTo(from.get("price"), maxPrice);
    }

    public static PredicateSpecification<Product> isInStock() {
        return (from, builder) -> builder.greaterThan(from.get("stockQuantity"), 0);
    }

    public static PredicateSpecification<Product> hasSearch(String search) {
        return (from, builder) ->
                builder.or(builder.like(builder.lower(from.get("name")), "%" + search.toLowerCase() + "%"),
                builder.like(builder.lower(from.get("description")), "%" + search.toLowerCase() + "%"));
    }
}
