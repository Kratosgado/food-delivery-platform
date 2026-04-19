package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.MenuItem;

public record MenuItemResponseDto(
    Long id,
    String name,
    String description,
    Integer price,
    String category,
    boolean available,
    String imageUrl) {
  public static MenuItemResponseDto fromEntity(MenuItem menuItem) {
    return new MenuItemResponseDto(
        menuItem.getId(),
        menuItem.getName(),
        menuItem.getDescription(),
        menuItem.getPrice(),
        menuItem.getCategory(),
        menuItem.isAvailable(),
        menuItem.getImageUrl());
  }
}
