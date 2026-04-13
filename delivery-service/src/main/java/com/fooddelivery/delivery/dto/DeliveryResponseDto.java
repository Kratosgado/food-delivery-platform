package com.fooddelivery.delivery.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record DeliveryResponseDto(
    Long id,
    Long orderId,
    String status,
    String driverName,
    String deliveryAddress,
    LocalDateTime assignedAt,
    LocalDateTime pickedUpAt,
    LocalDateTime deliveredAt
) {}
