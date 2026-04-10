package com.fooddelivery.customer.controller;

import com.fooddelivery.customer.dto.*;
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
    public ResponseEntity<CustomerResponseDto> register(@Valid @RequestBody CustomerRegistrationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto dto) {
        // TODO: migrate JWT login logic from monolith SecurityService / AuthController
        throw new UnsupportedOperationException("Implement JWT login — migrate from monolith");
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

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody CustomerRegistrationDto dto) {
        return ResponseEntity.ok(customerService.update(id, dto));
    }
}
