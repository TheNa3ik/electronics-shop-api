package com.thena3ik.shopapi.rest;

import com.thena3ik.shopapi.dto.common.PageResponse;
import com.thena3ik.shopapi.dto.order.OrderFilterRequest;
import com.thena3ik.shopapi.dto.order.OrderResponse;
import com.thena3ik.shopapi.dto.order.OrderStatusUpdateRequest;
import com.thena3ik.shopapi.dto.order.PlaceOrderRequest;
import com.thena3ik.shopapi.entity.OrderStatus;
import com.thena3ik.shopapi.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping()
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BigDecimal minTotalPrice,
            @RequestParam(required = false) BigDecimal maxTotalPrice,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            Pageable pageable) {

        OrderFilterRequest filter = new OrderFilterRequest(status, userId, minTotalPrice, maxTotalPrice, fromDate, toDate);
        PageResponse<OrderResponse> orders = orderService.getAllOrders(filter, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(Pageable pageable) {
        PageResponse<OrderResponse> orders = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId,
                                                           @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request.status());
        return ResponseEntity.ok(response);
    }
}
