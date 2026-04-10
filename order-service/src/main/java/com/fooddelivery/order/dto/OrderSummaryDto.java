package com.fooddelivery.order.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class OrderSummaryDto {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
}
