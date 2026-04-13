package com.fooddelivery.restaurant.client.dto;

import lombok.Builder;

@Builder
public record CustomerResponseDto(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone,
    String address,
    String role,
    String city) {}

