package com.thena3ik.shopapi.rest;

import com.thena3ik.shopapi.dto.order.OrderResponse;
import com.thena3ik.shopapi.dto.order.OrderStatusUpdateRequest;
import com.thena3ik.shopapi.dto.order.PlaceOrderRequest;
import com.thena3ik.shopapi.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        List<OrderResponse> orders = orderService.getMyOrders();
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
