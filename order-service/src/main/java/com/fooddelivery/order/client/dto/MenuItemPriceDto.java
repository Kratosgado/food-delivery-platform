package com.fooddelivery.order.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuItemPriceDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long restaurantId;
    private boolean available;
}
