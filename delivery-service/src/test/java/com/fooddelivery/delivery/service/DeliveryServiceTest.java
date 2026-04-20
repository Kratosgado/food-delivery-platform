package com.fooddelivery.delivery.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fooddelivery.delivery.dto.DeliveryResponseDto;
import com.fooddelivery.delivery.dto.UpdateDeliveryStatusDto;
import com.fooddelivery.delivery.exception.ResourceNotFoundException;
import com.fooddelivery.delivery.messaging.DeliveryEventPublisher;
import com.fooddelivery.delivery.model.Delivery;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryService Tests")
class DeliveryServiceTest {

  @Mock private DeliveryRepository deliveryRepository;
  @Mock private DeliveryEventPublisher eventPublisher;

  @InjectMocks private DeliveryService deliveryService;

  private static final Long DELIVERY_ID = 1L;
  private static final Long ORDER_ID = 100L;
  private static final String DRIVER_NAME = "John Driver";
  private static final Long DRIVER_ID = 500L;

  @BeforeEach
  void setUp() {}

  @Test
  @DisplayName("should retrieve delivery by id")
  void testGetByIdSuccess() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PENDING)
            .driverName(DRIVER_NAME)
            .driverId(DRIVER_ID)
            .createdAt(LocalDateTime.now())
            .build();

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));

    DeliveryResponseDto result = deliveryService.getById(DELIVERY_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(DELIVERY_ID);
    assertThat(result.orderId()).isEqualTo(ORDER_ID);
    assertThat(result.status()).isEqualTo(Delivery.DeliveryStatus.PENDING);

    verify(deliveryRepository).findById(DELIVERY_ID);
  }

  @Test
  @DisplayName("should throw exception when delivery not found by id")
  void testGetByIdNotFound() {
    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.getById(DELIVERY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Delivery");
  }

  @Test
  @DisplayName("should retrieve delivery by order id")
  void testGetByOrderIdSuccess() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PENDING)
            .driverName(DRIVER_NAME)
            .build();

    when(deliveryRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(delivery));

    DeliveryResponseDto result = deliveryService.getByOrderId(ORDER_ID);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(DELIVERY_ID);
    assertThat(result.orderId()).isEqualTo(ORDER_ID);

    verify(deliveryRepository).findByOrderId(ORDER_ID);
  }

  @Test
  @DisplayName("should throw exception when delivery not found by order id")
  void testGetByOrderIdNotFound() {
    when(deliveryRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.getByOrderId(ORDER_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Delivery not found for orderId");
  }

  @Test
  @DisplayName("should retrieve deliveries by status")
  void testGetByStatus() {
    Delivery delivery1 =
        Delivery.builder().id(1L).orderId(100L).status(Delivery.DeliveryStatus.PENDING).build();

    Delivery delivery2 =
        Delivery.builder().id(2L).orderId(101L).status(Delivery.DeliveryStatus.PENDING).build();

    when(deliveryRepository.findByStatus(Delivery.DeliveryStatus.PENDING))
        .thenReturn(List.of(delivery1, delivery2));

    List<DeliveryResponseDto> result = deliveryService.getByStatus("PENDING");

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(d -> d.status().equals(Delivery.DeliveryStatus.PENDING.name()));

    verify(deliveryRepository).findByStatus(Delivery.DeliveryStatus.PENDING);
  }

  @Test
  @DisplayName("should update delivery status to ASSIGNED")
  void testUpdateStatusToAssigned() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PENDING)
            .build();

    UpdateDeliveryStatusDto updateDto =
        new UpdateDeliveryStatusDto(Delivery.DeliveryStatus.ASSIGNED, DRIVER_NAME, DRIVER_ID);

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.updateStatus(DELIVERY_ID, updateDto);

    assertThat(result.status()).isEqualTo(Delivery.DeliveryStatus.ASSIGNED);
    assertThat(delivery.getAssignedAt()).isNotNull();

    verify(deliveryRepository).save(delivery);
    verify(eventPublisher).publishStatusUpdated(any());
  }

  @Test
  @DisplayName("should update delivery status to PICKED_UP")
  void testUpdateStatusToPickedUp() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.ASSIGNED)
            .driverName(DRIVER_NAME)
            .driverId(DRIVER_ID)
            .assignedAt(LocalDateTime.now())
            .build();

    UpdateDeliveryStatusDto updateDto =
        new UpdateDeliveryStatusDto(Delivery.DeliveryStatus.PICKED_UP, DRIVER_NAME, DRIVER_ID);

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.updateStatus(DELIVERY_ID, updateDto);

    assertThat(result.status()).isEqualTo(Delivery.DeliveryStatus.PICKED_UP);
    assertThat(delivery.getPickedUpAt()).isNotNull();

    verify(deliveryRepository).save(delivery);
    verify(eventPublisher).publishStatusUpdated(any());
  }

  @Test
  @DisplayName("should update delivery status to DELIVERED")
  void testUpdateStatusToDelivered() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PICKED_UP)
            .driverName(DRIVER_NAME)
            .driverId(DRIVER_ID)
            .pickedUpAt(LocalDateTime.now())
            .build();

    UpdateDeliveryStatusDto updateDto =
        new UpdateDeliveryStatusDto(Delivery.DeliveryStatus.DELIVERED, DRIVER_NAME, DRIVER_ID);

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.updateStatus(DELIVERY_ID, updateDto);

    assertThat(result.status()).isEqualTo(Delivery.DeliveryStatus.DELIVERED);
    assertThat(delivery.getDeliveredAt()).isNotNull();

    verify(deliveryRepository).save(delivery);
    verify(eventPublisher).publishStatusUpdated(any());
  }

  @Test
  @DisplayName("should update delivery with driver info")
  void testUpdateDeliveryWithDriverInfo() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PENDING)
            .build();

    UpdateDeliveryStatusDto updateDto =
        new UpdateDeliveryStatusDto(Delivery.DeliveryStatus.ASSIGNED, "Jane Smith", 501L);

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.updateStatus(DELIVERY_ID, updateDto);

    assertThat(delivery.getDriverName()).isEqualTo("Jane Smith");
    assertThat(delivery.getDriverId()).isEqualTo(501L);

    verify(deliveryRepository).save(delivery);
  }

  @Test
  @DisplayName("should cancel delivery")
  void testCancelDelivery() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.ASSIGNED)
            .driverName(DRIVER_NAME)
            .build();

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.cancelDelivery(DELIVERY_ID);

    assertThat(result.status()).isEqualTo(Delivery.DeliveryStatus.FAILED);

    verify(deliveryRepository).save(delivery);
  }

  @Test
  @DisplayName("should throw exception when cancelling non-existent delivery")
  void testCancelDeliveryNotFound() {
    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.cancelDelivery(DELIVERY_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Delivery");
  }

  @Test
  @DisplayName("should handle status update with null driver name")
  void testUpdateStatusWithNullDriverName() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PENDING)
            .driverName("Old Driver")
            .build();

    UpdateDeliveryStatusDto updateDto =
        new UpdateDeliveryStatusDto(Delivery.DeliveryStatus.ASSIGNED, null, DRIVER_ID);

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.updateStatus(DELIVERY_ID, updateDto);

    assertThat(delivery.getDriverName()).isEqualTo("Old Driver");
    assertThat(delivery.getDriverId()).isEqualTo(DRIVER_ID);

    verify(deliveryRepository).save(delivery);
  }

  @Test
  @DisplayName("should handle status update with null driver id")
  void testUpdateStatusWithNullDriverId() {
    Delivery delivery =
        Delivery.builder()
            .id(DELIVERY_ID)
            .orderId(ORDER_ID)
            .status(Delivery.DeliveryStatus.PENDING)
            .driverId(999L)
            .build();

    UpdateDeliveryStatusDto updateDto =
        new UpdateDeliveryStatusDto(Delivery.DeliveryStatus.ASSIGNED, DRIVER_NAME, null);

    when(deliveryRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));
    when(deliveryRepository.save(delivery))
        .thenAnswer(
            invocation -> {
              Delivery d = invocation.getArgument(0);
              return d;
            });

    DeliveryResponseDto result = deliveryService.updateStatus(DELIVERY_ID, updateDto);

    assertThat(delivery.getDriverName()).isEqualTo(DRIVER_NAME);
    assertThat(delivery.getDriverId()).isEqualTo(999L);

    verify(deliveryRepository).save(delivery);
  }
}
