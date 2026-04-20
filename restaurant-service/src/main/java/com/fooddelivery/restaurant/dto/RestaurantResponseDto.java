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

  public static RestaurantResponseDto fromEntity(
      Restaurant r, List<MenuItemResponseDto> menuItems) {
    return RestaurantResponseDto.builder()
        .id(r.getId())
        .name(r.getName())
        .description(r.getDescription())
        .cuisineType(r.getCuisineType())
        .address(r.getAddress())
        .city(r.getCity())
        .phone(r.getPhone())
        .active(r.isActive())
        .rating(r.getRating())
        .estimatedDeliveryMinutes(r.getEstimatedDeliveryMinutes())
        .menuItemCount(menuItems.size())
        .ownerId(r.getOwnerId())
        .menuItems(menuItems)
        .build();
  }

  public static RestaurantResponseDto fromEntity(Restaurant r) {
    return fromEntity(r, List.of());
  }
}
