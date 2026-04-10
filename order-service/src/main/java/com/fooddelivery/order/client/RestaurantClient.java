package com.fooddelivery.order.client;

import com.fooddelivery.order.client.dto.MenuItemPriceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service", fallback = RestaurantClientFallback.class)
public interface RestaurantClient {

    @GetMapping("/api/restaurants/menu-items/{menuItemId}/price")
    MenuItemPriceDto getMenuItemPrice(@PathVariable Long menuItemId);
}
