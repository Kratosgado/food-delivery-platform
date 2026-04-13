package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.MenuItem;
import java.math.BigDecimal;

public record MenuItemResponseDto(
    Long itemId,
    String itemName,
    String itemDescription,
    BigDecimal itemPrice,
    String itemCategory,
    boolean itemAvailable,
    String imageUrl
) {
    public static MenuItemResponseDto fromEntity(MenuItem menuItem) {
        return new MenuItemResponseDto(
            menuItem.getId(),
            menuItem.getName(),
            menuItem.getDescription(),
            menuItem.getPrice(),
            menuItem.getCategory(),
            menuItem.isAvailable(),
            menuItem.getImageUrl()
        );
    }
}
