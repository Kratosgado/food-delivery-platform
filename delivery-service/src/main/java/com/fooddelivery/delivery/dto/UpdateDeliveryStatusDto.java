package com.fooddelivery.delivery.dto;

import com.fooddelivery.delivery.model.Delivery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDeliveryStatusDto {
    @NotNull private Delivery.DeliveryStatus status;
    private String driverName;
    private Long driverId;
}
