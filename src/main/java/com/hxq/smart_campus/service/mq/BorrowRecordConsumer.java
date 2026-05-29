package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.entity.dto.BorrowMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
@Slf4j
@RequiredArgsConstructor
public class BorrowRecordConsumer {

    @RabbitListener(queues = "borrow-record-queue", containerFactory = "borrowListenerContainerFactory")
    public void handleBorrowMessage(BorrowMessage msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费借阅消息: borrowNo={}, eventType={}", msg.getBorrowNo(), msg.getEventType());

            if ("BORROW".equals(msg.getEventType())) {
                log.info("借阅通知: userId={}, bookId={}, borrowNo={}", msg.getUserId(), msg.getBookId(), msg.getBorrowNo());
            } else if ("RETURN".equals(msg.getEventType())) {
                log.info("归还通知: userId={}, bookId={}, borrowNo={}", msg.getUserId(), msg.getBookId(), msg.getBorrowNo());
            }

            channel.basicAck(deliveryTag, false);
            log.info("消费借阅消息成功: borrowNo={}", msg.getBorrowNo());
        } catch (Exception e) {
            log.error("消费借阅消息失败: borrowNo={}", msg.getBorrowNo(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }
}