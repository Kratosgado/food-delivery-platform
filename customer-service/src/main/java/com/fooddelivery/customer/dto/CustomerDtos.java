package com.fooddelivery.customer.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CustomerDtos() {

  public record UpdateCustomerDto(
      @Nullable @Size(min = 3, max = 50) String username,
      @Nullable @Email String email,
      @Nullable @Size(min = 6) String password,
      String firstName,
      String lastName,
      String phone,
      String deliveryAddress,
      String city) {}
}
