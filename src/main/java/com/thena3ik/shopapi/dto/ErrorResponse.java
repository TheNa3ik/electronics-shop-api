package com.thena3ik.shopapi.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    Integer status,
    String message,
    LocalDateTime timestamp) {
}
