package com.thena3ik.shopapi.specification;

import com.thena3ik.shopapi.entity.Order;
import com.thena3ik.shopapi.entity.OrderStatus;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderSpecification {

    private OrderSpecification () {}

    public static PredicateSpecification<Order> hasStatus(OrderStatus status) {
        return (from, builder) -> builder.equal(from.get("status"), status);
    }

    public static PredicateSpecification<Order> hasUserId(Long userId) {
        return (from, builder) -> builder.equal(from.get("user").get("id"), userId);
    }

    public static PredicateSpecification<Order> hasMinTotalPrice(BigDecimal minTotalPrice) {
        return (from, builder) -> builder.greaterThanOrEqualTo(from.get("totalPrice"), minTotalPrice);
    }

    public static PredicateSpecification<Order> hasMaxTotalPrice(BigDecimal maxTotalPrice) {
        return (from, builder) -> builder.lessThanOrEqualTo(from.get("totalPrice"), maxTotalPrice);
    }

    public static PredicateSpecification<Order> createdAfter(LocalDateTime fromDate) {
        return (from, builder) -> builder.greaterThanOrEqualTo(from.get("createdAt"), fromDate);
    }

    public static PredicateSpecification<Order> createdBefore(LocalDateTime toDate) {
        return (from, builder) -> builder.lessThanOrEqualTo(from.get("createdAt"), toDate);
    }
}
