package com.fooddelivery.customer.dto;


public record CustomerDtos() {

  public record UpdateCustomerDto(
      String firstName, String lastName, String phone, String deliveryAddress, String city) {}
}
