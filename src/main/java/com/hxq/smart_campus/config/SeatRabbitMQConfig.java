package com.hxq.smart_campus.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeatRabbitMQConfig {

    public static final String SEAT_EXCHANGE = "seat-reservation-exchange";
    public static final String SEAT_QUEUE = "seat-reservation-queue";
    public static final String SEAT_ROUTING_KEY = "reservation.create";

    @Bean
    public TopicExchange seatExchange() {
        return ExchangeBuilder.topicExchange(SEAT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue seatQueue() {
        return QueueBuilder.durable(SEAT_QUEUE)
                .withArgument("x-dead-letter-exchange", SEAT_EXCHANGE + "-dlx")
                .build();
    }

    @Bean
    public Binding seatBinding(@Qualifier("seatQueue") Queue seatQueue, @Qualifier("seatExchange") TopicExchange seatExchange) {
        return BindingBuilder.bind(seatQueue).to(seatExchange).with(SEAT_ROUTING_KEY);
    }
}
