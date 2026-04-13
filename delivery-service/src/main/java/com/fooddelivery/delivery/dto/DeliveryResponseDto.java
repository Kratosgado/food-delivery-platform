package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.Delivery;
import java.time.LocalDateTime;

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
    LocalDateTime createdAt
) {
    @SuppressWarnings("unused")
    private static class Builder {
        private Long id;
        private Long orderId;
        private String status;
        private String driverName;
        private String driverPhone;
        private String pickupAddress;
        private String deliveryAddress;
        private LocalDateTime assignedAt;
        private LocalDateTime pickedUpAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime createdAt;

        Builder id(Long id) { this.id = id; return this; }
        Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        Builder status(String status) { this.status = status; return this; }
        Builder driverName(String driverName) { this.driverName = driverName; return this; }
        Builder driverPhone(String driverPhone) { this.driverPhone = driverPhone; return this; }
        Builder pickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; return this; }
        Builder deliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; return this; }
        Builder assignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; return this; }
        Builder pickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; return this; }
        Builder deliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; return this; }
        Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public DeliveryResponseDto build() {
            return new DeliveryResponseDto(id, orderId, status, driverName, driverPhone,
                pickupAddress, deliveryAddress, assignedAt, pickedUpAt, deliveredAt, createdAt);
        }
    }

    public static Builder builder() { return new Builder(); }

    public static DeliveryResponseDto fromEntity(Delivery d) {
        return builder()
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
