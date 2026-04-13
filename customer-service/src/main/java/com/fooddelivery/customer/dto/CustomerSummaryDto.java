package com.fooddelivery.customer.dto;

import lombok.Builder;

/** Lightweight DTO returned to other services — never exposes password or sensitive fields. */
@Builder
public record CustomerSummaryDto(
    Long id,
    String fullName,
    String email,
    String deliveryAddress
) {}
