package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.config.SeatRabbitMQConfig;
import com.hxq.smart_campus.entity.dto.SeatReservationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "smartcampus.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class SeatReservationProducer {

    private final RabbitTemplate seatRabbitTemplate;

    public void sendReserveMessage(SeatReservationMessage message) {
        log.info("发送预约消息: reservationNo={}", message.getReservationNo());
        seatRabbitTemplate.convertAndSend(SeatRabbitMQConfig.SEAT_EXCHANGE,
                SeatRabbitMQConfig.SEAT_ROUTING_KEY, message);
    }

    public void sendCancelMessage(SeatReservationMessage message) {
        log.info("发送取消预约消息: reservationNo={}", message.getReservationNo());
        seatRabbitTemplate.convertAndSend(SeatRabbitMQConfig.SEAT_EXCHANGE,
                SeatRabbitMQConfig.SEAT_ROUTING_KEY, message);
    }

    public void sendCheckInMessage(SeatReservationMessage message) {
        log.info("发送签到消息: reservationNo={}", message.getReservationNo());
        seatRabbitTemplate.convertAndSend(SeatRabbitMQConfig.SEAT_EXCHANGE,
                SeatRabbitMQConfig.SEAT_ROUTING_KEY, message);
    }

    public void sendCheckOutMessage(SeatReservationMessage message) {
        log.info("发送签退消息: reservationNo={}", message.getReservationNo());
        seatRabbitTemplate.convertAndSend(SeatRabbitMQConfig.SEAT_EXCHANGE,
                SeatRabbitMQConfig.SEAT_ROUTING_KEY, message);
    }

    public void sendLeaveMessage(SeatReservationMessage message) {
        log.info("发送暂离消息: reservationNo={}", message.getReservationNo());
        seatRabbitTemplate.convertAndSend(SeatRabbitMQConfig.SEAT_EXCHANGE,
                SeatRabbitMQConfig.SEAT_ROUTING_KEY, message);
    }
}
