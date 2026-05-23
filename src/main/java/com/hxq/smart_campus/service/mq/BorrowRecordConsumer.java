package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.entity.dto.BorrowMessage;
import com.hxq.smart_campus.mapper.BookMapper;
import com.hxq.smart_campus.mapper.BorrowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rabbitmq.client.Channel;

import java.time.Duration;

import static com.hxq.smart_campus.constant.RedisConstant.MQ_PROCESSED_BORROW_PREFIX;


@Component
@Slf4j
@RequiredArgsConstructor
public class BorrowRecordConsumer {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration IDEMPOTENT_TTL = Duration.ofDays(1);

    @RabbitListener(queues = "borrow-record-queue")
    @Transactional
    public void handleBorrowMessage(BorrowMessage msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费借阅消息: borrowNo={}, eventType={}", msg.getBorrowNo(), msg.getEventType());

            String idempotentKey = MQ_PROCESSED_BORROW_PREFIX + msg.getBorrowNo();
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL);
            if (Boolean.FALSE.equals(acquired)) {
                log.warn("借阅消息已处理，跳过: borrowNo={}", msg.getBorrowNo());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if ("BORROW".equals(msg.getEventType())) {
                borrowRecordMapper.insertBorrowRecordDirect(msg.getUserId(), msg.getBookId(), msg.getBorrowNo());
                bookMapper.decrementAvailableCopies(msg.getBookId());
            } else if ("RETURN".equals(msg.getEventType())) {
                borrowRecordMapper.returnBookByBorrowNo(msg.getBorrowNo());
                bookMapper.incrementAvailableCopies(msg.getBookId());
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
