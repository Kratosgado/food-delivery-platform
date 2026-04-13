package com.fooddelivery.restaurant.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record RestaurantResponseDto(
    Long id,
    String name,
    String address,
    String cuisineType,
    boolean active,
    List<MenuItemPriceDto> menuItems
) {}
