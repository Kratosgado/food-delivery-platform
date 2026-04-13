package com.fooddelivery.order.client.dto;

import lombok.Builder;

@Builder
public record MenuItemPriceDto(
    Long id,
    String name,
    Integer price,
    Long restaurantId,
    boolean available
) {}
