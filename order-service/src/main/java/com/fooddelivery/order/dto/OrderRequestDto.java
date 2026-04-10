package com.fooddelivery.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull private Long customerId;
    @NotNull private Long restaurantId;
    @NotEmpty private List<OrderItemRequestDto> items;
    private String deliveryAddress; // overrides customer's default if provided

    @Data
    public static class OrderItemRequestDto {
        @NotNull private Long menuItemId;
        @NotNull private Integer quantity;
    }
}
