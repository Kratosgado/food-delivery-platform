package com.fooddelivery.restaurant.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fooddelivery.restaurant.client.CustomerClient;
import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.exception.UnauthorizedException;
import com.fooddelivery.restaurant.mapper.MenuItemMapper;
import com.fooddelivery.restaurant.model.MenuItem;
import com.fooddelivery.restaurant.model.Restaurant;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService Tests")
class RestaurantServiceTest {

  @Mock private RestaurantRepository restaurantRepository;
  @Mock private MenuItemRepository menuItemRepository;
  @Mock private MenuItemMapper menuItemMapper;
  @Mock private CustomerClient customerClient;

  @InjectMocks private RestaurantService restaurantService;

  private static final Long RESTAURANT_ID = 1L;
  private static final Long OWNER_ID = 100L;
  private static final Long MENU_ITEM_ID = 1000L;
  private static final String RESTAURANT_NAME = "Pizza Palace";
  private static final String CITY = "New York";
  private static final String CUISINE_TYPE = "Italian";

  @BeforeEach
  void setUp() {}

  @Test
  @DisplayName("should retrieve all active restaurants")
  void testFindAllActive() {
    Restaurant restaurant1 =
        Restaurant.builder()
            .id(1L)
            .name("Pizza Palace")
            .active(true)
            .cuisineType("Italian")
            .city(CITY)
            .build();

    Restaurant restaurant2 =
        Restaurant.builder()
            .id(2L)
            .name("Burger King")
            .active(true)
            .cuisineType("American")
            .city(CITY)
            .build();

    when(restaurantRepository.findByActiveTrue()).thenReturn(List.of(restaurant1, restaurant2));

    List<RestaurantResponseDto> result = restaurantService.findAll();

    assertThat(result).hasSize(2);
    verify(restaurantRepository).findByActiveTrue();
  }

  @Test
  @DisplayName("should retrieve restaurants by city")
  void testFindByCity() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .name(RESTAURANT_NAME)
            .city(CITY)
            .active(true)
            .cuisineType(CUISINE_TYPE)
            .build();

    when(restaurantRepository.findByCityIgnoreCaseAndActiveTrue(CITY))
        .thenReturn(List.of(restaurant));

    List<RestaurantResponseDto> result = restaurantService.findByCity(CITY);

    assertThat(result).hasSize(1);
    verify(restaurantRepository).findByCityIgnoreCaseAndActiveTrue(CITY);
  }

  @Test
  @DisplayName("should retrieve restaurants by cuisine type")
  void testFindByCuisine() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .name(RESTAURANT_NAME)
            .cuisineType(CUISINE_TYPE)
            .active(true)
            .city(CITY)
            .build();

    when(restaurantRepository.findByCuisineTypeIgnoreCaseAndActiveTrue(CUISINE_TYPE))
        .thenReturn(List.of(restaurant));

    List<RestaurantResponseDto> result = restaurantService.findByCuisine(CUISINE_TYPE);

    assertThat(result).hasSize(1);
    verify(restaurantRepository).findByCuisineTypeIgnoreCaseAndActiveTrue(CUISINE_TYPE);
  }

  @Test
  @DisplayName("should retrieve restaurant by id")
  void testFindByIdSuccess() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .name(RESTAURANT_NAME)
            .cuisineType(CUISINE_TYPE)
            .city(CITY)
            .active(true)
            .ownerId(OWNER_ID)
            .build();

    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
    when(menuItemRepository.findByRestaurantIdAndAvailableTrue(RESTAURANT_ID))
        .thenReturn(List.of());

    RestaurantResponseDto result = restaurantService.findById(RESTAURANT_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(RESTAURANT_ID);
    assertThat(result.name()).isEqualTo(RESTAURANT_NAME);
    verify(restaurantRepository).findById(RESTAURANT_ID);
  }

  @Test
  @DisplayName("should throw exception when restaurant not found by id")
  void testFindByIdNotFound() {
    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> restaurantService.findById(RESTAURANT_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Restaurant not found");
  }

  @Test
  @DisplayName("should create restaurant successfully")
  void testCreateRestaurantSuccess() {
    RestaurantRequestDto requestDto =
        RestaurantRequestDto.builder()
            .name(RESTAURANT_NAME)
            .description("Great Italian food")
            .cuisineType(CUISINE_TYPE)
            .address("123 Main St")
            .city(CITY)
            .phone("5551234567")
            .estimatedDeliveryMinutes(30)
            .build();

    Restaurant restaurant = requestDto.toEntity(OWNER_ID);

    when(customerClient.getCustomerById(OWNER_ID))
        .thenReturn(new CustomerResponseDto(OWNER_ID, "owner", "owner@example.com", "CUSTOMER"));
    when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

    RestaurantResponseDto result = restaurantService.createRestaurant(OWNER_ID, requestDto);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo(RESTAURANT_NAME);

    verify(customerClient).getCustomerById(OWNER_ID);
    verify(restaurantRepository).save(any(Restaurant.class));
  }

  @Test
  @DisplayName("should add menu item to restaurant")
  void testAddMenuItemSuccess() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .name(RESTAURANT_NAME)
            .ownerId(OWNER_ID)
            .build();

    MenuItemRequestDto itemRequest =
        MenuItemRequestDto.builder()
            .name("Margherita Pizza")
            .description("Classic pizza")
            .price(15)
            .category("Pizza")
            .build();

    MenuItem menuItem =
        MenuItem.builder()
            .id(MENU_ITEM_ID)
            .restaurant(restaurant)
            .name("Margherita Pizza")
            .description("Classic pizza")
            .price(15)
            .category("Pizza")
            .available(true)
            .build();

    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
    when(menuItemMapper.toEntity(itemRequest, restaurant)).thenReturn(menuItem);
    when(menuItemRepository.save(menuItem)).thenReturn(menuItem);

    MenuItemResponseDto result = restaurantService.addMenuItem(RESTAURANT_ID, OWNER_ID, itemRequest);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("Margherita Pizza");
    verify(menuItemRepository).save(menuItem);
  }

  @Test
  @DisplayName("should throw exception when non-owner tries to add menu item")
  void testAddMenuItemUnauthorized() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .name(RESTAURANT_NAME)
            .ownerId(OWNER_ID)
            .build();

    MenuItemRequestDto itemRequest =
        MenuItemRequestDto.builder()
            .name("Pizza")
            .price(15)
            .build();

    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

    assertThatThrownBy(
            () -> restaurantService.addMenuItem(RESTAURANT_ID, 999L, itemRequest))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("You don't own this restaurant");

    verify(menuItemRepository, never()).save(any());
  }

  @Test
  @DisplayName("should retrieve restaurant menu")
  void testGetMenu() {
    MenuItem item1 =
        MenuItem.builder()
            .id(1L)
            .name("Pizza")
            .price(15)
            .available(true)
            .build();

    MenuItem item2 =
        MenuItem.builder()
            .id(2L)
            .name("Pasta")
            .price(12)
            .available(true)
            .build();

    when(menuItemRepository.findByRestaurantIdAndAvailableTrue(RESTAURANT_ID))
        .thenReturn(List.of(item1, item2));

    List<MenuItemResponseDto> result = restaurantService.getMenu(RESTAURANT_ID);

    assertThat(result).hasSize(2);
    verify(menuItemRepository).findByRestaurantIdAndAvailableTrue(RESTAURANT_ID);
  }

  @Test
  @DisplayName("should update menu item successfully")
  void testUpdateMenuItemSuccess() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .name(RESTAURANT_NAME)
            .ownerId(OWNER_ID)
            .build();

    MenuItem menuItem =
        MenuItem.builder()
            .id(MENU_ITEM_ID)
            .restaurant(restaurant)
            .name("Margherita Pizza")
            .price(15)
            .description("Original description")
            .build();

    MenuItemRequestDto updateRequest =
        MenuItemRequestDto.builder()
            .name("Premium Margherita")
            .price(18)
            .description("Updated description")
            .build();

    when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));
    when(menuItemRepository.save(menuItem)).thenReturn(menuItem);

    MenuItemResponseDto result =
        restaurantService.updateMenuItem(MENU_ITEM_ID, OWNER_ID, updateRequest);

    assertThat(result).isNotNull();
    verify(menuItemRepository).save(menuItem);
  }

  @Test
  @DisplayName("should throw exception when non-owner tries to update menu item")
  void testUpdateMenuItemUnauthorized() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .ownerId(OWNER_ID)
            .build();

    MenuItem menuItem =
        MenuItem.builder()
            .id(MENU_ITEM_ID)
            .restaurant(restaurant)
            .name("Pizza")
            .build();

    MenuItemRequestDto updateRequest =
        MenuItemRequestDto.builder()
            .name("Updated Pizza")
            .build();

    when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));

    assertThatThrownBy(
            () -> restaurantService.updateMenuItem(MENU_ITEM_ID, 999L, updateRequest))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("You don't own this restaurant");

    verify(menuItemRepository, never()).save(any());
  }

  @Test
  @DisplayName("should toggle menu item availability")
  void testToggleMenuItemAvailability() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .ownerId(OWNER_ID)
            .build();

    MenuItem menuItem =
        MenuItem.builder()
            .id(MENU_ITEM_ID)
            .restaurant(restaurant)
            .name("Pizza")
            .available(true)
            .build();

    when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));

    restaurantService.toggleMenuItemAvailability(MENU_ITEM_ID, OWNER_ID);

    assertThat(menuItem.isAvailable()).isFalse();
    verify(menuItemRepository).save(menuItem);
  }

  @Test
  @DisplayName("should throw exception when non-owner toggles menu item availability")
  void testToggleMenuItemAvailabilityUnauthorized() {
    Restaurant restaurant =
        Restaurant.builder()
            .id(RESTAURANT_ID)
            .ownerId(OWNER_ID)
            .build();

    MenuItem menuItem =
        MenuItem.builder()
            .id(MENU_ITEM_ID)
            .restaurant(restaurant)
            .name("Pizza")
            .available(true)
            .build();

    when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));

    assertThatThrownBy(
            () -> restaurantService.toggleMenuItemAvailability(MENU_ITEM_ID, 999L))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("You don't own this restaurant");

    verify(menuItemRepository, never()).save(any());
  }

  @Test
  @DisplayName("should retrieve menu item price")
  void testGetMenuItemPrice() {
    MenuItem menuItem =
        MenuItem.builder()
            .id(MENU_ITEM_ID)
            .name("Pizza")
            .price(15)
            .available(true)
            .build();

    MenuItemPriceDto priceDto =
        MenuItemPriceDto.builder()
            .id(MENU_ITEM_ID)
            .name("Pizza")
            .price(15)
            .available(true)
            .build();

    when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));
    when(menuItemMapper.toPriceDto(menuItem)).thenReturn(priceDto);

    MenuItemPriceDto result = restaurantService.getMenuItemPrice(MENU_ITEM_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(MENU_ITEM_ID);
    assertThat(result.price()).isEqualTo(15);
    verify(menuItemMapper).toPriceDto(menuItem);
  }

  @Test
  @DisplayName("should throw exception when menu item price not found")
  void testGetMenuItemPriceNotFound() {
    when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> restaurantService.getMenuItemPrice(MENU_ITEM_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("MenuItem not found");
  }
}
