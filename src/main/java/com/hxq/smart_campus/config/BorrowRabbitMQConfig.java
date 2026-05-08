package com.hxq.smart_campus.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BorrowRabbitMQConfig {

    public static final String BORROW_EXCHANGE = "borrow-record-exchange";
    public static final String BORROW_QUEUE = "borrow-record-queue";
    public static final String BORROW_ROUTING_KEY = "borrow.create";

    @Bean
    public TopicExchange borrowExchange() {
        return ExchangeBuilder.topicExchange(BORROW_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue borrowQueue() {
        return QueueBuilder.durable(BORROW_QUEUE)
                .withArgument("x-dead-letter-exchange", BORROW_EXCHANGE + "-dlx")
                .withArgument("x-dead-letter-routing-key", BORROW_ROUTING_KEY + ".dlq")
                .build();
    }

    @Bean
    public Binding borrowBinding(@Qualifier("borrowQueue") Queue borrowQueue, @Qualifier("borrowExchange") TopicExchange borrowExchange) {
        return BindingBuilder.bind(borrowQueue).to(borrowExchange).with(BORROW_ROUTING_KEY);
    }
}
