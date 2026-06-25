package com.hxq.smart_campus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
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
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "course-selection-exchange";
    public static final String QUEUE_NAME = "course-selection-queue";
    public static final String ROUTING_KEY = "selection.create";

    public static final String DLX_EXCHANGE_NAME = "course-selection-exchange-dlx";
    public static final String DLX_QUEUE_NAME = "course-selection-queue-dlx";
    public static final String DLX_ROUTING_KEY = "selection.create.dlq";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding binding(@Qualifier("queue") Queue queue, @Qualifier("topicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public TopicExchange dlxTopicExchange() {
        return new TopicExchange(DLX_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE_NAME).build();
    }

    @Bean
    public Binding dlxBinding(@Qualifier("dlxQueue") Queue dlxQueue, @Qualifier("dlxTopicExchange") TopicExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue).to(dlxExchange).with(DLX_ROUTING_KEY);
    }

    @Bean
    public MessageConverter courseSelectionMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate courseSelectionRabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter courseSelectionMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(courseSelectionMessageConverter);
        return template;
    }

    @Bean
    public RabbitListenerContainerFactory<?> courseSelectionContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter courseSelectionMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(courseSelectionMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}