package com.fooddelivery.delivery.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderSummaryDto {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
}
