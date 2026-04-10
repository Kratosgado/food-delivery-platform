package com.fooddelivery.order.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderCancelledEvent {
    private Long orderId;
    private Long customerId;
    private String reason;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
