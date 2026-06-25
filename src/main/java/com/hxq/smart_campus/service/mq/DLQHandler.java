package com.hxq.smart_campus.service.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 死信队列统一处理器
 * 将三个模块的死信监听从 @Configuration 类剥离到此处统一管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "smartcampus.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class DLQHandler {

    private static final String MODULE_COURSE = "选课";
    private static final String MODULE_BORROW = "借阅";
    private static final String MODULE_SEAT = "座位预约";

    @RabbitListener(queues = "course-selection-queue-dlx")
    public void handleCourseSelectionDLQ(Message message) {
        log.error("{}消息进入死信队列: {}", MODULE_COURSE, new String(message.getBody(), StandardCharsets.UTF_8));
    }

    @RabbitListener(queues = "borrow-record-queue-dlx")
    public void handleBorrowDLQ(Message message) {
        log.error("{}消息进入死信队列: {}", MODULE_BORROW, new String(message.getBody(), StandardCharsets.UTF_8));
    }

    @RabbitListener(queues = "seat-reservation-queue-dlx")
    public void handleSeatDLQ(Message message) {
        log.error("{}消息进入死信队列: {}", MODULE_SEAT, new String(message.getBody(), StandardCharsets.UTF_8));
    }
}