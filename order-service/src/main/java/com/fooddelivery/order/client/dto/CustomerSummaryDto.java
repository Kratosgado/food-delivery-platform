package com.fooddelivery.order.client.dto;

import lombok.Data;

@Data
public class CustomerSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private String deliveryAddress;
}
