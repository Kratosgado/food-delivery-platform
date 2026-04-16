package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.client.CustomerClient;
import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.exception.UnauthorizedException;
import com.fooddelivery.restaurant.mapper.MenuItemMapper;
import com.fooddelivery.restaurant.model.*;
import com.fooddelivery.restaurant.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

  private final RestaurantRepository restaurantRepository;
  private final MenuItemRepository menuItemRepository;
  private final MenuItemMapper menuItemMapper;
  private final CustomerClient customerClient;

  @Transactional(readOnly = true)
  public List<RestaurantResponseDto> findAll() {
    return restaurantRepository.findByActiveTrue().stream()
        .map(RestaurantResponseDto::fromEntity)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RestaurantResponseDto> findByCity(String city) {
    return restaurantRepository.findByCityIgnoreCaseAndActiveTrue(city).stream()
        .map(RestaurantResponseDto::fromEntity)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RestaurantResponseDto> findByCuisine(String cuisineType) {
    return restaurantRepository.findByCuisineTypeIgnoreCaseAndActiveTrue(cuisineType).stream()
        .map(RestaurantResponseDto::fromEntity)
        .toList();
  }

  @Transactional(readOnly = true)
  public RestaurantResponseDto findById(Long id) {
    return toDto(findOrThrow(id));
  }

  public RestaurantResponseDto createRestaurant(Long ownerId, RestaurantRequestDto request) {
    var owner = customerClient.getCustomerById(ownerId);
    if (owner.role() != "RESTAURANT_OWNER") {
      customerClient.makeRestaurantOwner(ownerId);
    }
    var restaurant = request.toEntity(ownerId);
    restaurant.setOwnerId(ownerId);
    restaurant.setActive(true);
    restaurant.setRating(0.0);

    return RestaurantResponseDto.fromEntity(restaurantRepository.save(restaurant));
  }

  public MenuItemResponseDto addMenuItem(
      Long restaurantId, Long ownerId, MenuItemRequestDto request) {
    Restaurant restaurant = findOrThrow(restaurantId);

    if (!restaurant.getOwnerId().equals(ownerId)) {
      throw new UnauthorizedException("You don't own this restaurant");
    }

    MenuItem menuItem = menuItemMapper.toEntity(request, restaurant);
    MenuItem saved = menuItemRepository.save(menuItem);
    return MenuItemResponseDto.fromEntity(saved);
  }

  @Transactional(readOnly = true)
  public List<MenuItemResponseDto> getMenu(Long restaurantId) {
    return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
        .map(MenuItemResponseDto::fromEntity)
        .toList();
  }

  public MenuItemResponseDto updateMenuItem(Long itemId, Long ownerId, MenuItemRequestDto request) {
    MenuItem item =
        menuItemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("MenuItem not found: " + itemId));

    if (!item.getRestaurant().getOwnerId().equals(ownerId)) {
      throw new UnauthorizedException("You don't own this restaurant");
    }

    if (request.name() != null) item.setName(request.name());
    if (request.description() != null) item.setDescription(request.description());
    if (request.price() != null) item.setPrice(request.price());
    if (request.category() != null) item.setCategory(request.category());

    return MenuItemResponseDto.fromEntity(menuItemRepository.save(item));
  }

  public void toggleMenuItemAvailability(Long itemId, Long ownerId) {
    MenuItem item =
        menuItemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException("MenuItem not found: " + itemId));

    if (!item.getRestaurant().getOwnerId().equals(ownerId)) {
      throw new UnauthorizedException("You don't own this restaurant");
    }

    item.setAvailable(!item.isAvailable());
    menuItemRepository.save(item);
  }

  @Transactional(readOnly = true)
  public MenuItemPriceDto getMenuItemPrice(Long menuItemId) {
    MenuItem item =
        menuItemRepository
            .findById(menuItemId)
            .orElseThrow(() -> new EntityNotFoundException("MenuItem not found: " + menuItemId));
    return menuItemMapper.toPriceDto(item);
  }

  private Restaurant findOrThrow(Long id) {
    return restaurantRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + id));
  }

  private RestaurantResponseDto toDto(Restaurant r) {
    List<MenuItemResponseDto> menuItems =
        menuItemRepository.findByRestaurantIdAndAvailableTrue(r.getId()).stream()
            .map(MenuItemResponseDto::fromEntity)
            .toList();

    String ownerName = null;
    if (r.getOwnerId() != null) {
      try {
        ownerName = customerClient.getCustomerById(r.getOwnerId()).username();
      } catch (Exception e) {
        ownerName = "Unknown";
      }
    }

    return new RestaurantResponseDto(
        r.getId(),
        r.getName(),
        r.getDescription(),
        r.getCuisineType(),
        r.getAddress(),
        r.getCity(),
        r.getPhone(),
        r.isActive(),
        r.getRating(),
        r.getEstimatedDeliveryMinutes(),
        menuItems.size(),
        r.getOwnerId(),
        menuItems);
  }
}
