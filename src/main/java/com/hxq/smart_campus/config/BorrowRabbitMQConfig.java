package com.hxq.smart_campus.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "smartcampus.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class BorrowRabbitMQConfig {

    public static final String BORROW_EXCHANGE = "borrow-record-exchange";
    public static final String BORROW_QUEUE = "borrow-record-queue";
    public static final String BORROW_ROUTING_KEY = "borrow.create";

    @Bean
    public MessageConverter borrowMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate borrowRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter borrowMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(borrowMessageConverter);
        return rabbitTemplate;
    }

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
