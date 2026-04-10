package com.fooddelivery.order.client;

import com.fooddelivery.order.client.dto.MenuItemPriceDto;
import org.springframework.stereotype.Component;

@Component
public class RestaurantClientFallback implements RestaurantClient {

    @Override
    public MenuItemPriceDto getMenuItemPrice(Long menuItemId) {
        throw new com.fooddelivery.order.exception.ServiceUnavailableException(
                "Restaurant Service is currently unavailable. Cannot validate menu item price.");
    }
}
