package com.fooddelivery.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "deliveries")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Delivery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;              // references order_db — no FK

    private Long driverId;
    private String driverName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING_ASSIGNMENT;

    private String deliveryAddress;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DeliveryStatus {
        PENDING_ASSIGNMENT, ASSIGNED, PICKED_UP, DELIVERED, FAILED
    }
}
