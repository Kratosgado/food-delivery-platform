package com.fooddelivery.order.controller;

import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderResponseDto> placeOrder(
      @Valid @RequestBody OrderRequestDto dto, @RequestHeader(name = "X-User-Id") Long userId) {
    return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(userId, dto));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getById(id));
  }

  /** Internal — consumed by Delivery Service via Feign */
  @GetMapping("/{id}/summary")
  public ResponseEntity<OrderSummaryDto> getSummary(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.getSummary(id));
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long id) {
    return ResponseEntity.ok(orderService.cancelOrder(id));
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<OrderResponseDto>> getCustomerOrders(@PathVariable Long customerId) {
    return ResponseEntity.ok(orderService.getCustomerOrders(customerId));
  }

  @GetMapping("/restaurant/{restaurantId}")
  public ResponseEntity<List<OrderResponseDto>> getRestaurantOrders(
      @PathVariable Long restaurantId) {
    return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponseDto> updateStatus(
      @PathVariable Long id, @RequestParam String status) {
    return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
  }
}
