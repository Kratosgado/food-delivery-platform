package com.fooddelivery.order.client;

import com.fooddelivery.order.client.dto.CustomerSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback implements CustomerClient {

    @Override
    public CustomerSummaryDto getCustomerSummary(Long id) {
        // Circuit breaker fallback — Customer Service is unavailable
        throw new com.fooddelivery.order.exception.ServiceUnavailableException(
                "Customer Service is currently unavailable. Please retry in a moment.");
    }
}
