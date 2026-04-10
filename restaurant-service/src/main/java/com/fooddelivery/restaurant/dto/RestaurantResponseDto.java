package com.fooddelivery.restaurant.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class RestaurantResponseDto {
    private Long id;
    private String name;
    private String address;
    private String cuisineType;
    private boolean active;
    private List<MenuItemPriceDto> menuItems;
}
