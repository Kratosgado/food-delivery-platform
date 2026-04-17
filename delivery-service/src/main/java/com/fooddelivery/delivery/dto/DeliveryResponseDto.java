package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.Delivery;
import java.time.LocalDateTime;

@lombok.Builder
public record DeliveryResponseDto(
    Long id,
    Long orderId,
    String status,
    String driverName,
    String driverPhone,
    String pickupAddress,
    String deliveryAddress,
    LocalDateTime assignedAt,
    LocalDateTime pickedUpAt,
    LocalDateTime deliveredAt,
    LocalDateTime createdAt) {

  public static DeliveryResponseDto fromEntity(Delivery d) {
    return DeliveryResponseDto.builder()
        .id(d.getId())
        .orderId(d.getOrderId())
        .status(d.getStatus().name())
        .driverName(d.getDriverName())
        .driverPhone(d.getDriverPhone())
        .pickupAddress(d.getPickupAddress())
        .deliveryAddress(d.getDeliveryAddress())
        .assignedAt(d.getAssignedAt())
        .pickedUpAt(d.getPickedUpAt())
        .deliveredAt(d.getDeliveredAt())
        .createdAt(d.getCreatedAt())
        .build();
  }
}
