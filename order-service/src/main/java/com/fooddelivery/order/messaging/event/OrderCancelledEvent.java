package com.fooddelivery.order.messaging.event;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record OrderCancelledEvent(
    Long orderId,
    Long customerId,
    String reason,
    LocalDateTime timestamp
) {
    public OrderCancelledEvent {
        if (timestamp == null) timestamp = LocalDateTime.now();
    }
}
