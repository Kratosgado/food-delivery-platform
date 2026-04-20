package com.fooddelivery.restaurant.dto;

import lombok.Builder;

/** Lightweight DTO returned to Order Service for price validation */
@Builder
public record MenuItemPriceDto(
    Long id, String name, Integer price, Long restaurantId, boolean available) {}

