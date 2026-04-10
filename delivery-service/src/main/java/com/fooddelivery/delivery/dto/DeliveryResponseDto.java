package com.fooddelivery.delivery.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class DeliveryResponseDto {
    private Long id;
    private Long orderId;
    private String status;
    private String driverName;
    private String deliveryAddress;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
