package com.thena3ik.shopapi.dto.order;

import com.thena3ik.shopapi.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderFilterRequest(
        OrderStatus status,
        Long userId,
        BigDecimal minTotalPrice,
        BigDecimal maxTotalPrice,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
