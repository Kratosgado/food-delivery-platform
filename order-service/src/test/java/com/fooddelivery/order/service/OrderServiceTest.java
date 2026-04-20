package com.fooddelivery.order.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fooddelivery.order.client.CustomerClient;
import com.fooddelivery.order.client.RestaurantClient;
import com.fooddelivery.order.client.dto.CustomerSummaryDto;
import com.fooddelivery.order.client.dto.MenuItemPriceDto;
import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.exception.ServiceUnavailableException;
import com.fooddelivery.order.messaging.OrderEventPublisher;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderItem;
import com.fooddelivery.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private CustomerClient customerClient;
  @Mock private RestaurantClient restaurantClient;
  @Mock private OrderEventPublisher eventPublisher;

  @InjectMocks private OrderService orderService;

  private static final Long CUSTOMER_ID = 1L;
  private static final Long RESTAURANT_ID = 100L;
  private static final Long MENU_ITEM_ID = 1000L;
  private static final Long ORDER_ID = 5000L;

  @BeforeEach
  void setUp() {}

  @Test
  @DisplayName("should place order successfully with valid customer and menu items")
  void testPlaceOrderSuccess() {
    var itemRequest = new OrderRequestDto.OrderItemDto(MENU_ITEM_ID, 2, null);
    var orderRequest =
        new OrderRequestDto(RESTAURANT_ID, List.of(itemRequest), "123 Main St", null);

    CustomerSummaryDto customer =
        CustomerSummaryDto.builder()
            .id(CUSTOMER_ID)
            .fullName("John Doe")
            .email("john@example.com")
            .deliveryAddress("456 Elm St")
            .build();

    MenuItemPriceDto menuItem =
        MenuItemPriceDto.builder()
            .id(MENU_ITEM_ID)
            .name("Burger")
            .price(15)
            .available(true)
            .build();

    Order savedOrder =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .deliveryAddress("123 Main St")
            .totalAmount(30)
            .status(Order.OrderStatus.PLACED)
            .createdAt(LocalDateTime.now())
            .items(
                List.of(
                    OrderItem.builder()
                        .menuItemId(MENU_ITEM_ID)
                        .menuItemName("Burger")
                        .quantity(2)
                        .unitPrice(15)
                        .subtotal(30)
                        .build()))
            .build();

    when(customerClient.getCustomerSummary(CUSTOMER_ID)).thenReturn(customer);
    when(restaurantClient.getMenuItemPrice(MENU_ITEM_ID)).thenReturn(menuItem);
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    OrderResponseDto result = orderService.placeOrder(CUSTOMER_ID, orderRequest);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(ORDER_ID);
    assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
    assertThat(result.restaurantId()).isEqualTo(RESTAURANT_ID);
    assertThat(result.deliveryAddress()).isEqualTo("123 Main St");
    assertThat(result.totalAmount()).isEqualTo(30);
    assertThat(result.status()).isEqualTo("PENDING");

    verify(customerClient).getCustomerSummary(CUSTOMER_ID);
    verify(restaurantClient).getMenuItemPrice(MENU_ITEM_ID);
    verify(orderRepository).save(any(Order.class));
    verify(eventPublisher).publishOrderPlaced(any());
  }

  @Test
  @DisplayName("should use customer delivery address when not provided")
  void testPlaceOrderUsesCustomerDeliveryAddress() {
    var itemRequest = new OrderRequestDto.OrderItemDto(MENU_ITEM_ID, 1, null);
    var orderRequest = new OrderRequestDto(RESTAURANT_ID, List.of(itemRequest), null, null);

    CustomerSummaryDto customer =
        CustomerSummaryDto.builder()
            .id(CUSTOMER_ID)
            .fullName("John Doe")
            .email("john@example.com")
            .deliveryAddress("456 Elm St")
            .build();

    MenuItemPriceDto menuItem =
        MenuItemPriceDto.builder().id(MENU_ITEM_ID).name("Pizza").price(20).available(true).build();

    Order savedOrder =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .deliveryAddress("456 Elm St")
            .totalAmount(20)
            .status(Order.OrderStatus.PLACED)
            .createdAt(LocalDateTime.now())
            .items(
                List.of(
                    OrderItem.builder()
                        .menuItemId(MENU_ITEM_ID)
                        .menuItemName("Pizza")
                        .quantity(1)
                        .unitPrice(20)
                        .subtotal(20)
                        .build()))
            .build();

    when(customerClient.getCustomerSummary(CUSTOMER_ID)).thenReturn(customer);
    when(restaurantClient.getMenuItemPrice(MENU_ITEM_ID)).thenReturn(menuItem);
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    OrderResponseDto result = orderService.placeOrder(CUSTOMER_ID, orderRequest);

    assertThat(result.deliveryAddress()).isEqualTo("456 Elm St");
  }

  @Test
  @DisplayName("should throw exception when menu item not available")
  void testPlaceOrderMenuItemUnavailable() {
    var itemRequest = new OrderRequestDto.OrderItemDto(MENU_ITEM_ID, 1, null);
    var orderRequest =
        new OrderRequestDto(RESTAURANT_ID, List.of(itemRequest), "123 Main St", null);

    CustomerSummaryDto customer =
        CustomerSummaryDto.builder()
            .id(CUSTOMER_ID)
            .fullName("John Doe")
            .email("john@example.com")
            .deliveryAddress("456 Elm St")
            .build();

    MenuItemPriceDto menuItem =
        MenuItemPriceDto.builder()
            .id(MENU_ITEM_ID)
            .name("Burger")
            .price(15)
            .available(false)
            .build();

    when(customerClient.getCustomerSummary(CUSTOMER_ID)).thenReturn(customer);
    when(restaurantClient.getMenuItemPrice(MENU_ITEM_ID)).thenReturn(menuItem);

    assertThatThrownBy(() -> orderService.placeOrder(CUSTOMER_ID, orderRequest))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Menu item not available");
  }

  @Test
  @DisplayName("should throw ServiceUnavailableException when customer service unavailable")
  void testPlaceOrderCustomerServiceUnavailable() {
    var itemRequest = new OrderRequestDto.OrderItemDto(MENU_ITEM_ID, 1, null);
    var orderRequest =
        new OrderRequestDto(RESTAURANT_ID, List.of(itemRequest), "123 Main St", null);

    when(customerClient.getCustomerSummary(CUSTOMER_ID))
        .thenThrow(new RuntimeException("Service unavailable"));

    assertThatThrownBy(() -> orderService.placeOrder(CUSTOMER_ID, orderRequest))
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessageContaining("Customer Service unavailable");
  }

  @Test
  @DisplayName("should retrieve order by id")
  void testGetByIdSuccess() {
    Order order =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .status(Order.OrderStatus.PLACED)
            .totalAmount(50)
            .deliveryAddress("123 Main St")
            .createdAt(LocalDateTime.now())
            .items(List.of())
            .build();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    OrderResponseDto result = orderService.getById(ORDER_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(ORDER_ID);
    verify(orderRepository).findById(ORDER_ID);
  }

  @Test
  @DisplayName("should throw exception when order not found")
  void testGetByIdNotFound() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getById(ORDER_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Order not found");
  }

  @Test
  @DisplayName("should retrieve customer orders")
  void testGetCustomerOrders() {
    Order order1 = Order.builder().id(1L).customerId(CUSTOMER_ID).build();
    Order order2 = Order.builder().id(2L).customerId(CUSTOMER_ID).build();

    when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(CUSTOMER_ID))
        .thenReturn(List.of(order1, order2));

    List<OrderResponseDto> result = orderService.getCustomerOrders(CUSTOMER_ID);

    assertThat(result).hasSize(2);
    verify(orderRepository).findByCustomerIdOrderByCreatedAtDesc(CUSTOMER_ID);
  }

  @Test
  @DisplayName("should retrieve restaurant orders")
  void testGetRestaurantOrders() {
    Order order1 = Order.builder().id(1L).restaurantId(RESTAURANT_ID).build();
    Order order2 = Order.builder().id(2L).restaurantId(RESTAURANT_ID).build();

    when(orderRepository.findByRestaurantIdOrderByCreatedAtDesc(RESTAURANT_ID))
        .thenReturn(List.of(order1, order2));

    List<OrderResponseDto> result = orderService.getRestaurantOrders(RESTAURANT_ID);

    assertThat(result).hasSize(2);
    verify(orderRepository).findByRestaurantIdOrderByCreatedAtDesc(RESTAURANT_ID);
  }

  @Test
  @DisplayName("should update order status")
  void testUpdateOrderStatus() {
    Order order =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .status(Order.OrderStatus.PLACED)
            .build();

    Order updatedOrder =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .status(Order.OrderStatus.CONFIRMED)
            .updatedAt(LocalDateTime.now())
            .build();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(updatedOrder);

    OrderResponseDto result = orderService.updateOrderStatus(ORDER_ID, "CONFIRMED");

    assertThat(result.status()).isEqualTo("CONFIRMED");
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("should retrieve order summary")
  void testGetSummary() {
    Order order =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .status(Order.OrderStatus.PLACED)
            .totalAmount(100)
            .deliveryAddress("123 Main St")
            .build();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    OrderSummaryDto result = orderService.getSummary(ORDER_ID);

    assertThat(result.id()).isEqualTo(ORDER_ID);
    assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
    assertThat(result.restaurantId()).isEqualTo(RESTAURANT_ID);
    assertThat(result.status()).isEqualTo("PENDING");
    assertThat(result.totalAmount()).isEqualTo(100);
  }

  @Test
  @DisplayName("should cancel order and publish event")
  void testCancelOrderSuccess() {
    Order order =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .status(Order.OrderStatus.PLACED)
            .build();

    Order cancelledOrder =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .status(Order.OrderStatus.CANCELLED)
            .updatedAt(LocalDateTime.now())
            .build();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderRepository.save(order)).thenReturn(cancelledOrder);

    OrderResponseDto result = orderService.cancelOrder(ORDER_ID);

    assertThat(result.status()).isEqualTo("CANCELLED");
    verify(orderRepository).save(order);
    verify(eventPublisher).publishOrderCancelled(any());
  }

  @Test
  @DisplayName("should throw exception when cancelling delivered order")
  void testCancelDeliveredOrderFails() {
    Order order =
        Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .status(Order.OrderStatus.DELIVERED)
            .build();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot cancel a delivered order");

    verify(orderRepository, never()).save(any());
    verify(eventPublisher, never()).publishOrderCancelled(any());
  }
}
