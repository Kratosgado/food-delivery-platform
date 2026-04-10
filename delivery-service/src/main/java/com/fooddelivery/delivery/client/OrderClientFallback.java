package com.fooddelivery.delivery.client;

import com.fooddelivery.delivery.client.dto.OrderSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderSummaryDto getOrderSummary(Long id) {
        log.warn("OrderService unavailable — returning empty summary for orderId={}", id);
        // Graceful degradation: delivery assignment proceeds without full order enrichment
        OrderSummaryDto fallback = new OrderSummaryDto();
        fallback.setId(id);
        fallback.setStatus("UNKNOWN");
        return fallback;
    }
}
