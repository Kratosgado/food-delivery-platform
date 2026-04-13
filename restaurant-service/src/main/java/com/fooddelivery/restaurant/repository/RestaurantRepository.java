package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByActiveTrue();
    List<Restaurant> findByCuisineTypeIgnoreCaseAndActiveTrue(String cuisineType);
    List<Restaurant> findByCityIgnoreCaseAndActiveTrue(String city);
    Optional<Restaurant> findByIdAndOwnerId(Long id, Long ownerId);
}
