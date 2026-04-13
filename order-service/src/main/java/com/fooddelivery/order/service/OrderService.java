package com.fooddelivery.order.service;

import com.fooddelivery.order.client.CustomerClient;
import com.fooddelivery.order.client.RestaurantClient;
import com.fooddelivery.order.client.dto.CustomerSummaryDto;
import com.fooddelivery.order.client.dto.MenuItemPriceDto;
import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.messaging.OrderEventPublisher;
import com.fooddelivery.order.messaging.event.OrderCancelledEvent;
import com.fooddelivery.order.messaging.event.OrderPlacedEvent;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderItem;
import com.fooddelivery.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final CustomerClient customerClient;
  private final RestaurantClient restaurantClient;
  private final OrderEventPublisher eventPublisher;

  @CircuitBreaker(name = "customerService", fallbackMethod = "placeOrderCustomerFallback")
  // @CircuitBreaker(name = "restaurantService", fallbackMethod = "placeOrderRestaurantFallback")
  public OrderResponseDto placeOrder(Long userId, OrderRequestDto dto) {
    // 1. Validate customer via Feign (circuit-breaker protected)
    CustomerSummaryDto customer = customerClient.getCustomerSummary(userId);
    String deliveryAddress =
        dto.deliveryAddress() != null ? dto.deliveryAddress() : customer.deliveryAddress();

    // 2. Build order items — validate price via Feign
    Order order =
        Order.builder()
            .customerId(userId)
            .restaurantId(dto.restaurantId())
            .deliveryAddress(deliveryAddress)
            .build();

    List<OrderItem> items =
        dto.items().stream()
            .map(
                itemReq -> {
                  MenuItemPriceDto menuItem =
                      restaurantClient.getMenuItemPrice(itemReq.menuItemId());
                  if (!menuItem.available()) {
                    throw new IllegalStateException("Menu item not available: " + menuItem.name());
                  }
                  return OrderItem.builder()
                      .order(order)
                      .menuItemId(menuItem.id())
                      .menuItemName(menuItem.name())
                      .quantity(itemReq.quantity())
                      .unitPrice(menuItem.price())
                      .build();
                })
            .toList();

    order.setItems(items);
    order.setTotalAmount(
        items.stream().map(i -> i.getUnitPrice() * i.getQuantity()).reduce(0, Integer::sum));

    Order saved = orderRepository.save(order);

    // 3. Publish async event — Delivery Service will consume this
    eventPublisher.publishOrderPlaced(
        OrderPlacedEvent.builder()
            .orderId(saved.getId())
            .customerId(saved.getCustomerId())
            .restaurantId(saved.getRestaurantId())
            .deliveryAddress(saved.getDeliveryAddress())
            .totalAmount(saved.getTotalAmount())
            .build());

    return toResponseDto(saved);
  }

  // Circuit breaker fallback methods
  public OrderResponseDto placeOrderCustomerFallback(OrderRequestDto dto, Throwable t) {
    log.warn("CustomerService circuit breaker tripped: {}", t.getMessage());
    throw new com.fooddelivery.order.exception.ServiceUnavailableException(
        "Customer Service unavailable — cannot validate customer. Please retry.");
  }

  public OrderResponseDto placeOrderRestaurantFallback(OrderRequestDto dto, Throwable t) {
    log.warn("RestaurantService circuit breaker tripped: {}", t.getMessage());
    throw new com.fooddelivery.order.exception.ServiceUnavailableException(
        "Restaurant Service unavailable — cannot validate menu items. Please retry.");
  }

  @Transactional(readOnly = true)
  public OrderResponseDto getById(Long id) {
    return toResponseDto(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public List<OrderResponseDto> getCustomerOrders(Long customerId) {
    return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(this::toResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<OrderResponseDto> getRestaurantOrders(Long restaurantId) {
    return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
        .map(this::toResponseDto)
        .toList();
  }

  public OrderResponseDto updateOrderStatus(Long id, String status) {
    Order order = findOrThrow(id);
    order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
    order.setUpdatedAt(LocalDateTime.now());
    return toResponseDto(orderRepository.save(order));
  }

  /** Consumed by Delivery Service via Feign */
  @Transactional(readOnly = true)
  public OrderSummaryDto getSummary(Long id) {
    Order o = findOrThrow(id);
    return OrderSummaryDto.builder()
        .id(o.getId())
        .customerId(o.getCustomerId())
        .restaurantId(o.getRestaurantId())
        .status(o.getStatus().name())
        .totalAmount(o.getTotalAmount())
        .deliveryAddress(o.getDeliveryAddress())
        .build();
  }

  public OrderResponseDto cancelOrder(Long id) {
    Order order = findOrThrow(id);
    if (order.getStatus() == Order.OrderStatus.DELIVERED) {
      throw new IllegalStateException("Cannot cancel a delivered order");
    }
    order.setStatus(Order.OrderStatus.CANCELLED);
    order.setUpdatedAt(LocalDateTime.now());
    Order saved = orderRepository.save(order);
    eventPublisher.publishOrderCancelled(
        OrderCancelledEvent.builder()
            .orderId(saved.getId())
            .customerId(saved.getCustomerId())
            .build());
    return toResponseDto(saved);
  }

  private Order findOrThrow(Long id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
  }

  private OrderResponseDto toResponseDto(Order o) {
    return OrderResponseDto.builder()
        .id(o.getId())
        .customerId(o.getCustomerId())
        .restaurantId(o.getRestaurantId())
        .status(o.getStatus().name())
        .totalAmount(o.getTotalAmount())
        .deliveryAddress(o.getDeliveryAddress())
        .createdAt(o.getCreatedAt())
        .items(
            o.getItems().stream()
                .map(
                    i ->
                        OrderResponseDto.OrderItemResponseDto.builder()
                            .menuItemId(i.getMenuItemId())
                            .menuItemName(i.getMenuItemName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .subtotal(i.getUnitPrice() * i.getQuantity())
                            .build())
                .toList())
        .build();
  }
}
