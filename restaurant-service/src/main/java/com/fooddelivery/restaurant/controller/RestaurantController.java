package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/search/all")
    public ResponseEntity<List<RestaurantResponseDto>> findAll() {
        return ResponseEntity.ok(restaurantService.findAll());
    }

    @GetMapping("/search/city/{city}")
    public ResponseEntity<List<RestaurantResponseDto>> findByCity(@PathVariable String city) {
        return ResponseEntity.ok(restaurantService.findByCity(city));
    }

    @GetMapping("/search/cuisine/{type}")
    public ResponseEntity<List<RestaurantResponseDto>> findByCuisine(@PathVariable String type) {
        return ResponseEntity.ok(restaurantService.findByCuisine(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponseDto>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenu(id));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponseDto> create(
            @RequestHeader(name = "X-User-Id") Long ownerId,
            @Valid @RequestBody RestaurantRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(ownerId, request));
    }

    @PostMapping("/{restaurantId}/menu")
    public ResponseEntity<MenuItemResponseDto> addMenuItem(
            @PathVariable Long restaurantId,
            @RequestHeader(name = "X-User-Id") Long ownerId,
            @Valid @RequestBody MenuItemRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.addMenuItem(restaurantId, ownerId, request));
    }

    @PutMapping("/menu/{itemId}")
    public ResponseEntity<MenuItemResponseDto> updateMenuItem(
            @PathVariable Long itemId,
            @RequestHeader(name = "X-User-Id") Long ownerId,
            @Valid @RequestBody MenuItemRequestDto request) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(itemId, ownerId, request));
    }

    @PatchMapping("/menu/{itemId}/toggle")
    public ResponseEntity<Void> toggleAvailability(
            @PathVariable Long itemId,
            @RequestHeader(name = "X-User-Id") Long ownerId) {
        restaurantService.toggleMenuItemAvailability(itemId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/menu-items/{menuItemId}/price")
    public ResponseEntity<MenuItemPriceDto> getMenuItemPrice(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(restaurantService.getMenuItemPrice(menuItemId));
    }
}