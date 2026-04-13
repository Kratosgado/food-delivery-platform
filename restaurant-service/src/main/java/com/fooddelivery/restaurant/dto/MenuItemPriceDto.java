package com.fooddelivery.restaurant.dto;

import lombok.Builder;
import java.math.BigDecimal;

/** Lightweight DTO returned to Order Service for price validation */
@Builder
public record MenuItemPriceDto(
    Long id,
    String name,
    BigDecimal price,
    Long restaurantId,
    boolean available
) {}