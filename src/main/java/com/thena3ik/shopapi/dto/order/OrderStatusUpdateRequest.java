package com.thena3ik.shopapi.dto.order;

import com.thena3ik.shopapi.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {
}
