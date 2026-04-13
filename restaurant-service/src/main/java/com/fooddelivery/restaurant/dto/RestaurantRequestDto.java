package com.fooddelivery.restaurant.dto;

import com.fooddelivery.restaurant.model.Restaurant;
import jakarta.validation.constraints.NotBlank;

public record RestaurantRequestDto(
    @NotBlank String name,
    String description,
    @NotBlank String cuisineType,
    @NotBlank String address,
    @NotBlank String city,
    String phone,
    int estimatedDeliveryMinutes,
    Long ownerId) {
  public Restaurant toEntity(Long ownerId) {
    return Restaurant.builder()
        .name(name)
        .description(description)
        .cuisineType(cuisineType)
        .address(address)
        .city(city)
        .phone(phone)
        .estimatedDeliveryMinutes(estimatedDeliveryMinutes)
        .ownerId(ownerId)
        .build();
  }
}
