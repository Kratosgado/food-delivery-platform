package com.fooddelivery.customer.dto;

import lombok.Builder;
import lombok.Data;

/** Lightweight DTO returned to other services — never exposes password or sensitive fields. */
@Data @Builder
public class CustomerSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private String deliveryAddress;
}
