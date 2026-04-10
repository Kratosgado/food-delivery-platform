package com.fooddelivery.restaurant.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/** Lightweight DTO returned to Order Service for price validation */
@Data @Builder
public class MenuItemPriceDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long restaurantId;
    private boolean available;
}
