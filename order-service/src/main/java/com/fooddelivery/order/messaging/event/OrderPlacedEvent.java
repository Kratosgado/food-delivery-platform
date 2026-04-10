package com.fooddelivery.order.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
