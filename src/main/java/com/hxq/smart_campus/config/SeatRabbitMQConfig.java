package com.hxq.smart_campus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SeatRabbitMQConfig {

    public static final String SEAT_EXCHANGE = "seat-reservation-exchange";
    public static final String SEAT_QUEUE = "seat-reservation-queue";
    public static final String SEAT_ROUTING_KEY = "reservation.create";

    public static final String SEAT_DLX_EXCHANGE = "seat-reservation-exchange-dlx";
    public static final String SEAT_DLX_QUEUE = "seat-reservation-queue-dlx";
    public static final String SEAT_DLX_ROUTING_KEY = "reservation.create.dlq";

    @Bean
    public MessageConverter seatMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitListenerContainerFactory<?> seatReservationContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter seatMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(seatMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    @Bean
    public RabbitTemplate seatRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter seatMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(seatMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange seatExchange() {
        return ExchangeBuilder.topicExchange(SEAT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue seatQueue() {
        return QueueBuilder.durable(SEAT_QUEUE)
                .withArgument("x-dead-letter-exchange", SEAT_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SEAT_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding seatBinding(@Qualifier("seatQueue") Queue seatQueue, @Qualifier("seatExchange") TopicExchange seatExchange) {
        return BindingBuilder.bind(seatQueue).to(seatExchange).with(SEAT_ROUTING_KEY);
    }

    @Bean
    public TopicExchange seatDlxExchange() {
        return ExchangeBuilder.topicExchange(SEAT_DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue seatDlxQueue() {
        return QueueBuilder.durable(SEAT_DLX_QUEUE).build();
    }

    @Bean
    public Binding seatDlxBinding(@Qualifier("seatDlxQueue") Queue seatDlxQueue, @Qualifier("seatDlxExchange") TopicExchange seatDlxExchange) {
        return BindingBuilder.bind(seatDlxQueue).to(seatDlxExchange).with(SEAT_DLX_ROUTING_KEY);
    }

    @RabbitListener(queues = SEAT_DLX_QUEUE)
    public void handleDlxMessage(Message message) {
        log.error("座位预约消息进入死信队列: {}", new String(message.getBody()));
    }
}
