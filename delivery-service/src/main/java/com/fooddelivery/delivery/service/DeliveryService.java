package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.DeliveryResponseDto;
import com.fooddelivery.delivery.dto.UpdateDeliveryStatusDto;
import com.fooddelivery.delivery.messaging.DeliveryEventPublisher;
import com.fooddelivery.delivery.messaging.event.DeliveryStatusUpdatedEvent;
import com.fooddelivery.delivery.model.Delivery;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public DeliveryResponseDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public DeliveryResponseDto getByOrderId(Long orderId) {
        Delivery d = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found for orderId: " + orderId));
        return toDto(d);
    }

    public DeliveryResponseDto updateStatus(Long id, UpdateDeliveryStatusDto dto) {
        Delivery delivery = findOrThrow(id);
        delivery.setStatus(dto.status());
        if (dto.driverName() != null) delivery.setDriverName(dto.driverName());
        if (dto.driverId()   != null) delivery.setDriverId(dto.driverId());

        switch (dto.status()) {
            case ASSIGNED    -> delivery.setAssignedAt(LocalDateTime.now());
            case PICKED_UP   -> delivery.setPickedUpAt(LocalDateTime.now());
            case DELIVERED   -> delivery.setDeliveredAt(LocalDateTime.now());
            default          -> {}
        }

        Delivery saved = deliveryRepository.save(delivery);

        // Publish event so Order Service (and others) can react
        eventPublisher.publishStatusUpdated(DeliveryStatusUpdatedEvent.builder()
                .deliveryId(saved.getId()).orderId(saved.getOrderId())
                .status(saved.getStatus()).driverName(saved.getDriverName())
                .build());

        return toDto(saved);
    }

    private Delivery findOrThrow(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found: " + id));
    }

    private DeliveryResponseDto toDto(Delivery d) {
        return DeliveryResponseDto.builder()
                .id(d.getId()).orderId(d.getOrderId()).status(d.getStatus().name())
                .driverName(d.getDriverName()).deliveryAddress(d.getDeliveryAddress())
                .assignedAt(d.getAssignedAt()).pickedUpAt(d.getPickedUpAt())
                .deliveredAt(d.getDeliveredAt()).build();
    }
}
