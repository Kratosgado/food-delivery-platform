package com.fooddelivery.order.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponseDto {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private List<OrderItemResponseDto> items;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDateTime createdAt;

    @Data @Builder
    public static class OrderItemResponseDto {
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
