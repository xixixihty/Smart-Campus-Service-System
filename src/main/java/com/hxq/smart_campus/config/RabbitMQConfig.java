package com.hxq.smart_campus.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;  
import org.springframework.amqp.core.Queue;  

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "course-selection-exchange";
    public static final String QUEUE_NAME = "course-selection-queue";
    public static final String ROUTING_KEY = "selection.create";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME)
        .withArgument("x-dead-letter-exchange", EXCHANGE_NAME + "-dlx")
        .build();
    }
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}
