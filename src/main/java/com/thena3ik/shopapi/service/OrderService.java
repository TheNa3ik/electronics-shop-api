package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.order.OrderItemRequest;
import com.thena3ik.shopapi.dto.order.OrderItemResponse;
import com.thena3ik.shopapi.dto.order.OrderResponse;
import com.thena3ik.shopapi.dto.order.PlaceOrderRequest;
import com.thena3ik.shopapi.entity.*;
import com.thena3ik.shopapi.exception.AccessDeniedException;
import com.thena3ik.shopapi.exception.InsufficientStockException;
import com.thena3ik.shopapi.exception.InvalidOrderStatusTransitionException;
import com.thena3ik.shopapi.exception.ResourceNotFoundException;
import com.thena3ik.shopapi.repository.OrderRepository;
import com.thena3ik.shopapi.repository.ProductRepository;
import com.thena3ik.shopapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSku(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemResponses);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING -> {
                if (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED)
                    return true;
            }
            case CONFIRMED -> {
                if (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED)
                    return true;
            }
            case SHIPPED -> {
                if (newStatus == OrderStatus.DELIVERED)
                    return true;
            }
            default -> {
                return false;
            }
        }
        return false;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = getCurrentUser();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.productId()));

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);

        BigDecimal totalPrice = orderItems
                .stream()
                .map(item -> item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        return toOrderResponse(savedOrder);
    }

    @Transactional
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();

        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        User user = getCurrentUser();

        if (user.getRole() == Role.CUSTOMER) {
            orderRepository.findByIdAndUserId(orderId, user.getId())
                    .orElseThrow(() -> new AccessDeniedException("You do not have access to this order"));
        }

        return toOrderResponse(order);
    }

    @Transactional
    public List<OrderResponse> getAllOrders () {
        return orderRepository.findAll()
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new InvalidOrderStatusTransitionException("Cannot transition order from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        return toOrderResponse(order);
    }
}
