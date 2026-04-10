package com.fooddelivery.order.client;

import com.fooddelivery.order.client.dto.CustomerSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", fallback = CustomerClientFallback.class)
public interface CustomerClient {

    @GetMapping("/api/customers/{id}/summary")
    CustomerSummaryDto getCustomerSummary(@PathVariable Long id);
}
