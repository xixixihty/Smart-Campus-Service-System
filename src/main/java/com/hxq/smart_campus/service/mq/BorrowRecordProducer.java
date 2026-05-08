package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.config.BorrowRabbitMQConfig;
import com.hxq.smart_campus.entity.dto.BorrowMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BorrowRecordProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendBorrowMessage(BorrowMessage message) {
        log.info("发送借阅消息: borrowNo={}, bookId={}, userId={}", message.getBorrowNo(), message.getBookId(), message.getUserId());
        rabbitTemplate.convertAndSend(BorrowRabbitMQConfig.BORROW_EXCHANGE,
                BorrowRabbitMQConfig.BORROW_ROUTING_KEY, message);
    }

    public void sendReturnMessage(BorrowMessage message) {
        log.info("发送归还消息: borrowNo={}, bookId={}, userId={}", message.getBorrowNo(), message.getBookId(), message.getUserId());
        rabbitTemplate.convertAndSend(BorrowRabbitMQConfig.BORROW_EXCHANGE,
                BorrowRabbitMQConfig.BORROW_ROUTING_KEY, message);
    }
}
