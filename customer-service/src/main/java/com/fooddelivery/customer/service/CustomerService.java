package com.fooddelivery.customer.service;

import com.fooddelivery.customer.dto.*;
import com.fooddelivery.customer.dto.CustomerDtos.UpdateCustomerDto;
import com.fooddelivery.customer.exception.DuplicateResourceException;
import com.fooddelivery.customer.model.Customer;
import com.fooddelivery.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final PasswordEncoder passwordEncoder;
  private final com.fooddelivery.customer.util.JwtUtil jwtUtil;

  public AuthResponseDto login(LoginDto dto) {
    Customer customer =
        customerRepository
            .findByEmail(dto.email())
            .orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("Customer not found"));

    if (!passwordEncoder.matches(dto.password(), customer.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtUtil.generateToken(customer);

    return AuthResponseDto.builder()
        .token(token)
        .tokenType("Bearer")
        .customer(CustomerResponseDto.fromEntity(customer))
        .build();
  }

  public AuthResponseDto register(CustomerRegistrationDto dto) {

    if (customerRepository.existsByUsername(dto.username())) {
      throw new DuplicateResourceException("Username already taken");
    }
    if (customerRepository.existsByEmail(dto.email())) {
      throw new DuplicateResourceException("Email already registered");
    }

    Customer customer =
        Customer.builder()
            .username(dto.username())
            .firstName(dto.firstName())
            .lastName(dto.lastName())
            .email(dto.email())
            .password(passwordEncoder.encode(dto.password()))
            .phone(dto.phone())
            .deliveryAddress(dto.deliveryAddress())
            .city(dto.city())
            .role(Customer.Role.CUSTOMER)
            .build();

    customerRepository.save(customer);
    String token = jwtUtil.generateToken(customer);
    return AuthResponseDto.builder()
        .token(token)
        .tokenType("Bearer")
        .customer(CustomerResponseDto.fromEntity(customer))
        .build();
  }

  @Transactional(readOnly = true)
  public CustomerResponseDto getById(Long id) {
    return CustomerResponseDto.fromEntity(findOrThrow(id));
  }

  /** Used by Order Service via Feign — returns minimal delivery info */
  @Transactional(readOnly = true)
  public CustomerSummaryDto getSummary(Long id) {
    Customer c = findOrThrow(id);
    return CustomerSummaryDto.builder()
        .id(c.getId())
        .fullName(c.getFirstName() + " " + c.getLastName())
        .email(c.getEmail())
        .deliveryAddress(c.getDeliveryAddress())
        .build();
  }

  public CustomerResponseDto update(Long id, UpdateCustomerDto dto) {
    Customer customer = findOrThrow(id);

    if (dto.firstName() != null) customer.setFirstName(dto.firstName());
    if (dto.lastName() != null) customer.setLastName(dto.lastName());
    if (dto.phone() != null) customer.setPhone(dto.phone());
    if (dto.deliveryAddress() != null) customer.setDeliveryAddress(dto.deliveryAddress());
    if (dto.city() != null) customer.setCity(dto.city());

    return CustomerResponseDto.fromEntity(customerRepository.save(customer));
  }

  public Customer findByEmail(String email) {
    return customerRepository
        .findByEmail(email)
        .orElseThrow(
            () -> new jakarta.persistence.EntityNotFoundException("Customer not found: " + email));
  }

  private Customer findOrThrow(Long id) {
    return customerRepository
        .findById(id)
        .orElseThrow(
            () -> new jakarta.persistence.EntityNotFoundException("Customer not found: " + id));
  }
}
