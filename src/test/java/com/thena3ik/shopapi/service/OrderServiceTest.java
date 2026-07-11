package com.thena3ik.shopapi.service;

import com.thena3ik.shopapi.dto.order.OrderItemRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@test.com");
        customer.setRole(Role.CUSTOMER);

        product = new Product();
        product.setId(1L);
        product.setName("Resistor");
        product.setSku("RES-1");
        product.setPrice(BigDecimal.valueOf(5.00));
        product.setStockQuantity(10);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(customer.getEmail(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        lenient().when(userRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));
    }

    @Test
    void placeOrder_whenStockIsSufficient_decrementsStockAndReturnsOrder() {
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        PlaceOrderRequest request = new PlaceOrderRequest(List.of(itemRequest));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(100L);
            return savedOrder;
        });

        OrderResponse result = orderService.placeOrder(request);

        assertThat(product.getStockQuantity()).isEqualTo(8);

        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(10.00));

        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().subtotal()).isEqualByComparingTo(BigDecimal.valueOf(10.00));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_whenStockIsInsufficient_throwsInsufficientStockException() {
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 999);
        PlaceOrderRequest request = new PlaceOrderRequest(List.of(itemRequest));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock for product: Resistor");

        verify(orderRepository, never()).save(any(Order.class));

        assertThat(product.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void placeOrder_whenProductDoesNotExist_throwsResourceNotFoundException() {
        OrderItemRequest itemRequest = new OrderItemRequest(999L, 1);
        PlaceOrderRequest request = new PlaceOrderRequest(List.of(itemRequest));

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_withMultipleItems_calculatesTotalPriceCorrectly() {
        Product secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setName("Capacitor");
        secondProduct.setSku("CAP-1");
        secondProduct.setPrice(BigDecimal.valueOf(2.50));
        secondProduct.setStockQuantity(20);

        OrderItemRequest item1 = new OrderItemRequest(1L, 2);
        OrderItemRequest item2 = new OrderItemRequest(2L, 4);
        PlaceOrderRequest request = new PlaceOrderRequest(List.of(item1, item2));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(2L)).thenReturn(Optional.of(secondProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse result = orderService.placeOrder(request);

        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
        assertThat(result.items()).hasSize(2);
    }

    @Test
    void getOrderById_whenCustomerOwnsOrder_returnsOrder() {
        Order order = new Order();
        order.setId(5L);
        order.setUser(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.valueOf(10.00));
        order.setItems(List.of());

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        when(orderRepository.findByIdAndUserId(5L, customer.getId()))
                .thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(5L);

        assertThat(result.id()).isEqualTo(5L);
    }

    @Test
    void getOrderById_whenCustomerDoesNotOwnOrder_throwsAccessDeniedException() {
        Order someoneElsesOrder = new Order();
        someoneElsesOrder.setId(5L);
        someoneElsesOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(someoneElsesOrder));

        when(orderRepository.findByIdAndUserId(5L, customer.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(5L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have access to this order");
    }

    @Test
    void getOrderById_whenOrderDoesNotExist_throwsResourceNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 999");
    }

    @Test
    void updateOrderStatus_whenTransitionIsValid_updatesStatus() {
        Order order = new Order();
        order.setId(5L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of());

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse result = orderService.updateOrderStatus(5L, OrderStatus.CONFIRMED);

        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateOrderStatus_whenTransitionIsInvalid_throwsInvalidOrderStatusTransitionException() {
        Order order = new Order();
        order.setId(5L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(5L, OrderStatus.DELIVERED))
                .isInstanceOf(InvalidOrderStatusTransitionException.class)
                .hasMessageContaining("Cannot transition order from PENDING to DELIVERED");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any(Order.class));
    }
}