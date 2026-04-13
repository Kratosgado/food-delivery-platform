package com.fooddelivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MenuItemRequestDto(
    @NotBlank String name,
    String description,
    @NotNull @Positive BigDecimal price,
    String category,
    String imageUrl
) {}
