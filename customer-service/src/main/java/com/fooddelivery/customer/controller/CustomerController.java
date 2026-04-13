package com.fooddelivery.customer.controller;

import com.fooddelivery.customer.dto.*;
import com.fooddelivery.customer.dto.CustomerDtos.UpdateCustomerDto;
import com.fooddelivery.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody CustomerRegistrationDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(dto));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
    return ResponseEntity.ok(customerService.login(dto));
  }

  @GetMapping("/me")
  public ResponseEntity<CustomerResponseDto> getCurrentUser(
      @RequestHeader(name = "X-User-Id") Long id) {
    return ResponseEntity.ok(customerService.getById(id));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(customerService.getById(id));
  }

  /** Internal endpoint consumed by Order Service via Feign */
  @GetMapping("/{id}/summary")
  public ResponseEntity<CustomerSummaryDto> getSummary(@PathVariable Long id) {
    return ResponseEntity.ok(customerService.getSummary(id));
  }

  @PutMapping("/me")
  public ResponseEntity<CustomerResponseDto> update(
      @RequestHeader(name = "X-User-Id") Long id, @Valid @RequestBody UpdateCustomerDto dto) {
    return ResponseEntity.ok(customerService.update(id, dto));
  }

  @PutMapping("/make-restaurant-owner/{id}")
  public ResponseEntity<Void> makeRestaurantOwner(@PathVariable Long id) {
    customerService.makeRestaurantOwner(id);
    return ResponseEntity.noContent().build();
  }
}
