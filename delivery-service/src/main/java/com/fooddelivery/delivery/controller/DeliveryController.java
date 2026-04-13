package com.fooddelivery.delivery.controller;

import com.fooddelivery.delivery.dto.DeliveryResponseDto;
import com.fooddelivery.delivery.dto.UpdateDeliveryStatusDto;
import com.fooddelivery.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  @GetMapping("/{id}")
  public ResponseEntity<DeliveryResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(deliveryService.getById(id));
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<DeliveryResponseDto> getByOrderId(@PathVariable Long orderId) {
    return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<DeliveryResponseDto>> getByStatus(@PathVariable String status) {
    return ResponseEntity.ok(deliveryService.getByStatus(status));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<DeliveryResponseDto> updateStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateDeliveryStatusDto dto) {
    return ResponseEntity.ok(deliveryService.updateStatus(id, dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancelDelivery(@PathVariable Long id) {
    deliveryService.cancelDelivery(id);
    return ResponseEntity.noContent().build();
  }
}
