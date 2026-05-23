package com.hxq.smart_campus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "smartcampus.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class BorrowRabbitMQConfig {

    public static final String BORROW_EXCHANGE = "borrow-record-exchange";
    public static final String BORROW_QUEUE = "borrow-record-queue";
    public static final String BORROW_ROUTING_KEY = "borrow.create";

    public static final String BORROW_DLX_EXCHANGE = "borrow-record-exchange-dlx";
    public static final String BORROW_DLX_QUEUE = "borrow-record-queue-dlx";
    public static final String BORROW_DLX_ROUTING_KEY = "borrow.create.dlq";

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
                .withArgument("x-dead-letter-exchange", BORROW_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", BORROW_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding borrowBinding(@Qualifier("borrowQueue") Queue borrowQueue, @Qualifier("borrowExchange") TopicExchange borrowExchange) {
        return BindingBuilder.bind(borrowQueue).to(borrowExchange).with(BORROW_ROUTING_KEY);
    }

    @Bean
    public TopicExchange borrowDlxExchange() {
        return ExchangeBuilder.topicExchange(BORROW_DLX_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue borrowDlxQueue() {
        return QueueBuilder.durable(BORROW_DLX_QUEUE).build();
    }

    @Bean
    public Binding borrowDlxBinding(@Qualifier("borrowDlxQueue") Queue borrowDlxQueue, @Qualifier("borrowDlxExchange") TopicExchange borrowDlxExchange) {
        return BindingBuilder.bind(borrowDlxQueue).to(borrowDlxExchange).with(BORROW_DLX_ROUTING_KEY);
    }

    @RabbitListener(queues = BORROW_DLX_QUEUE)
    public void handleDlxMessage(Message message) {
        log.error("借阅消息进入死信队列: {}", new String(message.getBody()));
    }
}
