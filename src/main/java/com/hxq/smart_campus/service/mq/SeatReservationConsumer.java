package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.entity.dto.SeatReservationMessage;
import com.hxq.smart_campus.mapper.SeatReservationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rabbitmq.client.Channel;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeatReservationConsumer {

    private final SeatReservationMapper seatReservationMapper;

    @RabbitListener(queues = "seat-reservation-queue")
    @Transactional
    public void handleSeatMessage(SeatReservationMessage msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费座位预约消息: reservationNo={}, eventType={}", msg.getReservationNo(), msg.getEventType());

            if ("RESERVE".equals(msg.getEventType())) {
                seatReservationMapper.insertReservationDirect(
                        msg.getUserId(), msg.getSeatId(), msg.getDate(),
                        msg.getStartTime(), msg.getEndTime(), msg.getReservationNo());
            } else if ("CANCEL".equals(msg.getEventType())) {
                seatReservationMapper.updateStatusByReservationNo(msg.getReservationNo(), "已取消");
            }

            channel.basicAck(deliveryTag, false);
            log.info("消费座位预约消息成功: reservationNo={}", msg.getReservationNo());
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
