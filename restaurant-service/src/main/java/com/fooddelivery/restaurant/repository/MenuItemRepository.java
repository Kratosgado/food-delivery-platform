package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
    List<MenuItem> findByRestaurantId(Long restaurantId);
    Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);
}
