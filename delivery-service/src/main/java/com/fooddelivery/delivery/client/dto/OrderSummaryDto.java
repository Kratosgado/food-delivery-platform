package com.fooddelivery.delivery.client.dto;

import lombok.Builder;

@Builder
public record OrderSummaryDto(
    Long id,
    Long customerId,
    Long restaurantId,
    String status,
    Integer totalAmount,
    String deliveryAddress
) {}
