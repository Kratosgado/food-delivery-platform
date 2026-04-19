package com.fooddelivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MenuItemRequestDto(
    @NotBlank String name,
    String description,
    @NotNull @Positive Integer price,
    String category,
    String imageUrl) {}
