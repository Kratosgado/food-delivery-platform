package com.fooddelivery.delivery.client;

import com.fooddelivery.delivery.client.dto.OrderSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {

    @GetMapping("/api/orders/{id}/summary")
    OrderSummaryDto getOrderSummary(@PathVariable Long id);
}
