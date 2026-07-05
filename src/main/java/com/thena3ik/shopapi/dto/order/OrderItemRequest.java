package com.thena3ik.shopapi.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull
        Long productId,

        @NotNull
        @Positive
        Integer quantity) {
}
