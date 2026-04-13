package com.fooddelivery.order.dto;

import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderResponseDto(
    Long id,
    Long customerId,
    Long restaurantId,
    List<OrderItemResponseDto> items,
    String status,
    Integer totalAmount,
    String deliveryAddress,
    LocalDateTime createdAt) {
  @Builder
  public record OrderItemResponseDto(
      Long menuItemId, String menuItemName, Integer quantity, Integer unitPrice, Integer subtotal) {
    public static OrderItemResponseDto fromEntity(OrderItem orderItem) {
      return OrderItemResponseDto.builder()
          .menuItemId(orderItem.getMenuItemId())
          .menuItemName(orderItem.getMenuItemName())
          .quantity(orderItem.getQuantity())
          .unitPrice(orderItem.getUnitPrice())
          .subtotal(orderItem.getSubtotal())
          .build();
    }
  }

  public static OrderResponseDto fromEntity(Order order) {
    return OrderResponseDto.builder()
        .id(order.getId())
        .customerId(order.getCustomerId())
        .restaurantId(order.getRestaurantId())
        .status(order.getStatus().name())
        .totalAmount(order.getTotalAmount())
        .deliveryAddress(order.getDeliveryAddress())
        .createdAt(order.getCreatedAt())
        .items(order.getItems().stream().map(OrderItemResponseDto::fromEntity).toList())
        .build();
  }
}
