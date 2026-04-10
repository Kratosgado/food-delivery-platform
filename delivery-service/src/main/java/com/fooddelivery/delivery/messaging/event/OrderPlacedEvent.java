package com.fooddelivery.delivery.messaging.event;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderPlacedEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}
