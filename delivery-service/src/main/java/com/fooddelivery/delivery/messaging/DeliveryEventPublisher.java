package com.fooddelivery.delivery.messaging;

import com.fooddelivery.delivery.config.RabbitMQConfig;
import com.fooddelivery.delivery.messaging.event.DeliveryStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
public class DeliveryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishStatusUpdated(DeliveryStatusUpdatedEvent event) {
        log.info("Publishing DeliveryStatusUpdatedEvent deliveryId={} status={}",
                event.getDeliveryId(), event.getStatus());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.DELIVERY_STATUS_ROUTING_KEY,
                event);
    }
}
