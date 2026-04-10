package com.fooddelivery.delivery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE                   = "food.delivery.exchange";
    public static final String DLX                        = "food.delivery.dlx";
    public static final String ORDER_PLACED_QUEUE         = "order.placed.queue";
    public static final String ORDER_PLACED_DLQ           = "order.placed.dlq";
    public static final String DELIVERY_STATUS_QUEUE      = "delivery.status.queue";
    public static final String ORDER_PLACED_ROUTING_KEY   = "order.placed";
    public static final String DELIVERY_STATUS_ROUTING_KEY = "delivery.status.updated";

    @Bean TopicExchange exchange() { return new TopicExchange(EXCHANGE); }
    @Bean TopicExchange dlx()      { return new TopicExchange(DLX); }

    @Bean
    Queue orderPlacedQueue() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", ORDER_PLACED_QUEUE + ".dlq")
                .build();
    }

    @Bean Queue orderPlacedDlq()      { return QueueBuilder.durable(ORDER_PLACED_DLQ).build(); }
    @Bean Queue deliveryStatusQueue() { return new Queue(DELIVERY_STATUS_QUEUE, true); }

    @Bean Binding orderPlacedBinding(Queue orderPlacedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPlacedQueue).to(exchange).with(ORDER_PLACED_ROUTING_KEY);
    }

    @Bean Binding deliveryStatusBinding(Queue deliveryStatusQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deliveryStatusQueue).to(exchange).with(DELIVERY_STATUS_ROUTING_KEY);
    }

    @Bean Jackson2JsonMessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(jsonConverter());
        return t;
    }
}
