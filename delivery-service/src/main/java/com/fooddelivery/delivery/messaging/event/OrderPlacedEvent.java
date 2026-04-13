package com.fooddelivery.delivery.messaging.event;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record OrderPlacedEvent(
    Long orderId,
    Long customerId,
    Long restaurantId,
    String deliveryAddress,
    Integer totalAmount,
    LocalDateTime timestamp
) {}
