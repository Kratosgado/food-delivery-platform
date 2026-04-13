package com.fooddelivery.customer.dto;

import lombok.Builder;

@Builder
public record AuthResponseDto(
    String token,
    String tokenType,
    CustomerResponseDto customer
) {}