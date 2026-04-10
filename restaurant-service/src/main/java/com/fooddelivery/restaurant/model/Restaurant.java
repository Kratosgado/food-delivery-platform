package com.fooddelivery.restaurant.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "restaurants")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Restaurant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String name;
    private String address;
    private String cuisineType;
    private String phone;
    private Long ownerId;         // references customer_db — stored as plain ID, no FK
    @Builder.Default private boolean active = true;
}
