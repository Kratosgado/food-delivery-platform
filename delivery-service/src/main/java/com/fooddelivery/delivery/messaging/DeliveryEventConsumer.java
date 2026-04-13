package com.fooddelivery.delivery.messaging;

import com.fooddelivery.delivery.messaging.event.OrderPlacedEvent;
import com.fooddelivery.delivery.model.Delivery;
import com.fooddelivery.delivery.repository.DeliveryRepository;
import com.fooddelivery.delivery.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class DeliveryEventConsumer {

    private final DeliveryRepository deliveryRepository;

    /**
     * Automatically creates a Delivery record when an order is placed.
     * Message failures retry 3x then route to DLQ (configured in application.yml).
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for orderId={}", event.orderId());

        if (deliveryRepository.findByOrderId(event.orderId()).isPresent()) {
            log.warn("Delivery already exists for orderId={} — skipping (idempotent)", event.orderId());
            return;
        }

        Delivery delivery = Delivery.builder()
                .orderId(event.orderId())
                .deliveryAddress(event.deliveryAddress())
                .status(Delivery.DeliveryStatus.PENDING)
                .build();

        deliveryRepository.save(delivery);
        log.info("Created delivery record id={} for orderId={}", delivery.getId(), event.orderId());
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_DLQ)
    public void onOrderPlacedDlq(OrderPlacedEvent event) {
        log.error("DLQ message received for orderId={} — requires manual investigation", event.orderId());
        // TODO: alert / store in dead-letter audit table
    }
}
