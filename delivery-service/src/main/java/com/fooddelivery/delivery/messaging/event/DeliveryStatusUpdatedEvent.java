package com.fooddelivery.delivery.messaging.event;

import com.fooddelivery.delivery.model.Delivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryStatusUpdatedEvent {
    private Long deliveryId;
    private Long orderId;
    private Delivery.DeliveryStatus status;
    private String driverName;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
