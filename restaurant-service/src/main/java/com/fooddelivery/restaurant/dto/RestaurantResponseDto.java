package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.Restaurant;
import java.util.List;
import lombok.Builder;

@Builder
public record RestaurantResponseDto(
    Long id,
    String name,
    String description,
    String cuisineType,
    String address,
    String city,
    String phone,
    boolean active,
    double rating,
    int estimatedDeliveryMinutes,
    int menuItemCount,
    Long ownerId,
    List<MenuItemResponseDto> menuItems) {
  public static RestaurantResponseDto fromEntity(Restaurant restaurant) {
    return RestaurantResponseDto.builder()
        .id(restaurant.getId())
        .name(restaurant.getName())
        .description(restaurant.getDescription())
        .cuisineType(restaurant.getCuisineType())
        .address(restaurant.getAddress())
        .city(restaurant.getCity())
        .phone(restaurant.getPhone())
        .active(restaurant.isActive())
        .rating(restaurant.getRating())
        .estimatedDeliveryMinutes(restaurant.getEstimatedDeliveryMinutes())
        .menuItemCount(0)
        .ownerId(restaurant.getOwnerId())
        .menuItems(null)
        .build();
  }
}
