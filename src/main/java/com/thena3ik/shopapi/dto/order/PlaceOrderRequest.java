package com.thena3ik.shopapi.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceOrderRequest(
        @NotNull
        @NotEmpty
        @Valid
        List<OrderItemRequest> items) {
}
