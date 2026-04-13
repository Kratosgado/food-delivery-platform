package com.fooddelivery.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequestDto(
    @NotNull Long customerId,
    @NotNull Long restaurantId,
    @NotEmpty List<OrderItemRequestDto> items,
    String deliveryAddress // overrides customer's default if provided
) {
    public record OrderItemRequestDto(
        @NotNull Long menuItemId,
        @NotNull Integer quantity
    ) {}
}
