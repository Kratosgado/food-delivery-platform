package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.MenuItemRequestDto;
import com.fooddelivery.restaurant.dto.MenuItemPriceDto;
import com.fooddelivery.restaurant.model.MenuItem;
import com.fooddelivery.restaurant.model.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {

    public MenuItem toEntity(MenuItemRequestDto dto, Restaurant restaurant) {
        return MenuItem.builder()
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .category(dto.category())
                .imageUrl(dto.imageUrl())
                .restaurant(restaurant)
                .build();
    }

    public MenuItemPriceDto toPriceDto(MenuItem menuItem) {
        return new MenuItemPriceDto(
            menuItem.getId(),
            menuItem.getName(),
            menuItem.getPrice(),
            menuItem.getRestaurant().getId(),
            menuItem.isAvailable()
        );
    }
}
