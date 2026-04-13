package com.fooddelivery.restaurant.client;

import com.fooddelivery.restaurant.client.dto.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "customer-service")
public interface CustomerClient {

  @GetMapping("/api/customers/{id}")
  CustomerResponseDto getCustomerById(@PathVariable Long id);

  @PutMapping("/api/customers/make-restaurant-owner/{id}")
  Void makeRestaurantOwner(@PathVariable Long id);
}
