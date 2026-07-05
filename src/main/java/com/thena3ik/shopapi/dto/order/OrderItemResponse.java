package com.thena3ik.shopapi.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal priceAtPurchase,
        BigDecimal subtotal) {
}
