package com.fooddelivery.customer.repository;

import com.fooddelivery.customer.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
