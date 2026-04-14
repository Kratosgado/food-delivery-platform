package com.fooddelivery.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequestDto(
    @NotNull Long restaurantId,
    @NotEmpty List<OrderItemDto> items,
    String deliveryAddress, // overrides customer's default if provided
    String specialInstructions) {
  public record OrderItemDto(
      @NotNull Long menuItemId, @NotNull Integer quantity, String specialInstructions) {}
}
