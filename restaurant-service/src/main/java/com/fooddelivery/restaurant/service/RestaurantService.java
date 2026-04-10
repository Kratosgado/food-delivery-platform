package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.model.*;
import com.fooddelivery.restaurant.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> findAll() {
        // TODO: migrate from monolith RestaurantService.findAll()
        return restaurantRepository.findByActiveTrue().stream()
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponseDto findById(Long id) {
        return toDto(findOrThrow(id));
    }

    /** Called by Order Service via Feign to validate price before order placement */
    @Transactional(readOnly = true)
    public MenuItemPriceDto getMenuItemPrice(Long menuItemId) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new EntityNotFoundException("MenuItem not found: " + menuItemId));
        return MenuItemPriceDto.builder()
                .id(item.getId()).name(item.getName()).price(item.getPrice())
                .restaurantId(item.getRestaurant().getId()).available(item.isAvailable())
                .build();
    }

    public RestaurantResponseDto createRestaurant(RestaurantResponseDto dto) {
        // TODO: migrate from monolith
        throw new UnsupportedOperationException("Implement — migrate from monolith");
    }

    private Restaurant findOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + id));
    }

    private RestaurantResponseDto toDto(Restaurant r) {
        List<MenuItemPriceDto> items = menuItemRepository
                .findByRestaurantIdAndAvailableTrue(r.getId()).stream()
                .map(i -> MenuItemPriceDto.builder()
                        .id(i.getId()).name(i.getName()).price(i.getPrice())
                        .restaurantId(r.getId()).available(i.isAvailable()).build())
                .toList();
        return RestaurantResponseDto.builder()
                .id(r.getId()).name(r.getName()).address(r.getAddress())
                .cuisineType(r.getCuisineType()).active(r.isActive()).menuItems(items).build();
    }
}
