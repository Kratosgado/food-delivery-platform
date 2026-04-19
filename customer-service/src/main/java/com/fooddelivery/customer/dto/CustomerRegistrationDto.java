package com.fooddelivery.customer.dto;

import com.fooddelivery.customer.model.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRegistrationDto(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    String firstName,
    String lastName,
    String phone,
    String deliveryAddress,
    String city) {
  public Customer toEntity() {
    return Customer.builder()
        .username(username)
        .email(email)
        .firstName(firstName)
        .lastName(lastName)
        .phone(phone)
        .deliveryAddress(deliveryAddress)
        .city(city)
        .role(Customer.Role.CUSTOMER)
        .build();
  }
}

