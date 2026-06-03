package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.entity.dto.SeatReservationMessage;
import com.hxq.smart_campus.mapper.SeatReservationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rabbitmq.client.Channel;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeatReservationConsumer {

    private final SeatReservationMapper seatReservationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration IDEMPOTENT_TTL = Duration.ofDays(1);
    private static final String MQ_PROCESSED_SEAT_PREFIX = "mq:processed:seat:";

    @RabbitListener(queues = "seat-reservation-queue", containerFactory = "seatReservationContainerFactory")
    @Transactional
    public void handleSeatMessage(SeatReservationMessage msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费座位预约消息: reservationNo={}, eventType={}",
                    msg.getReservationNo(), msg.getEventType());

            String idempotentKey = MQ_PROCESSED_SEAT_PREFIX
                    + msg.getReservationNo() + ":" + msg.getEventType();
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL);
            if (Boolean.FALSE.equals(acquired)) {
                log.warn("座位预约消息已处理，跳过: reservationNo={}, eventType={}",
                        msg.getReservationNo(), msg.getEventType());
                channel.basicAck(deliveryTag, false);
                return;
            }

            switch (msg.getEventType()) {
                case "RESERVE":
                    seatReservationMapper.insertReservationDirect(
                            msg.getUserId(), msg.getSeatId(), msg.getDate(),
                            msg.getStartTime(), msg.getEndTime(), msg.getReservationNo());
                    break;
                case "CANCEL":
                    seatReservationMapper.updateStatusByReservationNo(msg.getReservationNo(), "已取消");
                    break;
                case "CHECK_IN":
                    seatReservationMapper.checkInByReservationNo(msg.getReservationNo());
                    break;
                case "CHECK_OUT":
                    seatReservationMapper.checkOutByReservationNo(msg.getReservationNo());
                    break;
                case "LEAVE":
                    seatReservationMapper.leaveByReservationNo(msg.getReservationNo());
                    break;
                default:
                    log.warn("未知事件类型: {}", msg.getEventType());
            }

            channel.basicAck(deliveryTag, false);
            log.info("消费座位预约消息成功: reservationNo={}, eventType={}",
                    msg.getReservationNo(), msg.getEventType());
        } catch (Exception e) {
            log.error("消费座位预约消息失败: reservationNo={}", msg.getReservationNo(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }
}
