package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.RestaurantRequestDto;
import com.fooddelivery.restaurant.model.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {

    public Restaurant toEntity(RestaurantRequestDto dto) {
        return Restaurant.builder()
                .name(dto.name())
                .description(dto.description())
                .cuisineType(dto.cuisineType())
                .address(dto.address())
                .city(dto.city())
                .phone(dto.phone())
                .build();
    }

    public void updateEntityFromDto(RestaurantRequestDto dto, Restaurant entity) {
        if (dto.name() != null) entity.setName(dto.name());
        if (dto.description() != null) entity.setDescription(dto.description());
        if (dto.cuisineType() != null) entity.setCuisineType(dto.cuisineType());
        if (dto.address() != null) entity.setAddress(dto.address());
        if (dto.city() != null) entity.setCity(dto.city());
        if (dto.phone() != null) entity.setPhone(dto.phone());
    }
}
