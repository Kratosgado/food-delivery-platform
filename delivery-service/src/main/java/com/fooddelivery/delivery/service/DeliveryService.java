package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.DeliveryResponseDto;
import com.fooddelivery.delivery.dto.UpdateDeliveryStatusDto;
import com.fooddelivery.delivery.exception.ResourceNotFoundException;
import com.fooddelivery.delivery.messaging.DeliveryEventPublisher;
import com.fooddelivery.delivery.messaging.event.DeliveryStatusUpdatedEvent;
import com.fooddelivery.delivery.model.Delivery;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final DeliveryEventPublisher eventPublisher;

  @Transactional(readOnly = true)
  public DeliveryResponseDto getById(Long id) {
    return DeliveryResponseDto.fromEntity(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public DeliveryResponseDto getByOrderId(Long orderId) {
    Delivery d =
        deliveryRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () -> new EntityNotFoundException("Delivery not found for orderId: " + orderId));
    return DeliveryResponseDto.fromEntity(d);
  }

  @Transactional(readOnly = true)
  public List<DeliveryResponseDto> getByStatus(String status) {
    Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.valueOf(status.toUpperCase());
    return deliveryRepository.findByStatus(deliveryStatus).stream()
        .map(DeliveryResponseDto::fromEntity)
        .toList();
  }

  public DeliveryResponseDto updateStatus(Long id, UpdateDeliveryStatusDto dto) {
    Delivery delivery = findOrThrow(id);
    delivery.setStatus(dto.status());
    if (dto.driverName() != null) delivery.setDriverName(dto.driverName());
    if (dto.driverId() != null) delivery.setDriverId(dto.driverId());

    switch (dto.status()) {
      case ASSIGNED -> delivery.setAssignedAt(LocalDateTime.now());
      case PICKED_UP -> delivery.setPickedUpAt(LocalDateTime.now());
      case DELIVERED -> delivery.setDeliveredAt(LocalDateTime.now());
      default -> {}
    }

    Delivery saved = deliveryRepository.save(delivery);

    eventPublisher.publishStatusUpdated(
        DeliveryStatusUpdatedEvent.builder()
            .deliveryId(saved.getId())
            .orderId(saved.getOrderId())
            .status(saved.getStatus())
            .driverName(saved.getDriverName())
            .build());

    return DeliveryResponseDto.fromEntity(saved);
  }

  public DeliveryResponseDto cancelDelivery(Long deliveryId) {
    Delivery delivery = findOrThrow(deliveryId);
    delivery.setStatus(Delivery.DeliveryStatus.FAILED);
    Delivery saved = deliveryRepository.save(delivery);
    log.info("NOTIFICATION: Delivery #{} cancelled", deliveryId);
    return DeliveryResponseDto.fromEntity(saved);
  }

  private Delivery findOrThrow(Long id) {
    return deliveryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
  }
}
