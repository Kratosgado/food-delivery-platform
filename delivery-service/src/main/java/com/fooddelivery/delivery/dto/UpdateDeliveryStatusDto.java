package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.Delivery;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusDto(
    @NotNull Delivery.DeliveryStatus status,
    String driverName,
    Long driverId
) {}
