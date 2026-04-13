package com.fooddelivery.order.client.dto;

import lombok.Builder;

@Builder
public record CustomerSummaryDto(
    Long id,
    String fullName,
    String email,
    String deliveryAddress
) {}
