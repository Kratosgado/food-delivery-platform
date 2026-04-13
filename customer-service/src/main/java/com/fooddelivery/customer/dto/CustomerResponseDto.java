package com.fooddelivery.customer.dto;

import com.fooddelivery.customer.model.Customer;
import lombok.Builder;

@Builder
public record CustomerResponseDto(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone,
    String address,
    String role,
    String city) {
  public static CustomerResponseDto fromEntity(Customer customer) {
    return CustomerResponseDto.builder()
        .id(customer.getId())
        .username(customer.getUsername())
        .firstName(customer.getFirstName())
        .lastName(customer.getLastName())
        .email(customer.getEmail())
        .phone(customer.getPhone())
        .address(customer.getDeliveryAddress())
        .role(customer.getRole().name())
        .city(customer.getCity())
        .build();
  }
}
