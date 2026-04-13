package com.fooddelivery.order.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponseDto(
    Long id,
    Long customerId,
    Long restaurantId,
    List<OrderItemResponseDto> items,
    String status,
    Integer totalAmount,
    String deliveryAddress,
    LocalDateTime createdAt
) {
    @Builder
    public record OrderItemResponseDto(
        Long menuItemId,
        String menuItemName,
        Integer quantity,
        Integer unitPrice,
        Integer subtotal
    ) {}
}
