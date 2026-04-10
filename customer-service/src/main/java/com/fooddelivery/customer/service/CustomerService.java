package com.fooddelivery.customer.service;

import com.fooddelivery.customer.dto.*;
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

    public CustomerResponseDto register(CustomerRegistrationDto dto) {
        // TODO: migrate registration logic from monolith CustomerService
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        Customer customer = Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();
        return toResponseDto(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto getById(Long id) {
        return toResponseDto(findOrThrow(id));
    }

    /** Used by Order Service via Feign — returns minimal delivery info */
    @Transactional(readOnly = true)
    public CustomerSummaryDto getSummary(Long id) {
        Customer c = findOrThrow(id);
        return CustomerSummaryDto.builder()
                .id(c.getId())
                .fullName(c.getFirstName() + " " + c.getLastName())
                .email(c.getEmail())
                .deliveryAddress(c.getAddress())
                .build();
    }

    public CustomerResponseDto update(Long id, CustomerRegistrationDto dto) {
        // TODO: migrate update logic from monolith
        Customer customer = findOrThrow(id);
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        return toResponseDto(customerRepository.save(customer));
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Customer not found: " + email));
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Customer not found: " + id));
    }

    private CustomerResponseDto toResponseDto(Customer c) {
        return CustomerResponseDto.builder()
                .id(c.getId()).firstName(c.getFirstName()).lastName(c.getLastName())
                .email(c.getEmail()).phone(c.getPhone()).address(c.getAddress())
                .role(c.getRole().name()).build();
    }
}
