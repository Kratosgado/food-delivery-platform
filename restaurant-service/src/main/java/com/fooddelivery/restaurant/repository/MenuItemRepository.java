package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.model.MenuItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
  List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

  List<MenuItem> findByRestaurantId(Long restaurantId);

  Optional<MenuItem> findByIdAndRestaurantId(Long id, Long restaurantId);

  Optional<MenuItem> findByIdAndRestaurantOwnerId(Long id, Long ownerId);
}
