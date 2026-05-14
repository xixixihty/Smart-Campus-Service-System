package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.config.RabbitMQConfig;
import com.hxq.smart_campus.entity.dto.CourseSelectionCreateDTO;
import com.hxq.smart_campus.mapper.CourseSelectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourseSelectionConsumer {

    private final CourseSelectionMapper courseSelectionMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleCourseSelection(Map<String, Object> msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            Long studentId = ((Number) msg.get("studentId")).longValue();
            Long courseId = ((Number) msg.get("courseId")).longValue();
            Long semesterId = ((Number) msg.get("semesterId")).longValue();

            log.info("消费选课消息: studentId={}, courseId={}, semesterId={}", studentId, courseId, semesterId);

            CourseSelectionCreateDTO dto = new CourseSelectionCreateDTO();
            dto.setStudentId(studentId);
            dto.setCourseId(courseId);
            dto.setSemesterId(semesterId);

            courseSelectionMapper.insertCourseSelection(dto);
            log.info("选课记录已写入数据库: studentId={}, courseId={}", studentId, courseId);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消费选课消息失败: msg={}", msg, e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("拒绝选课消息失败", ex);
            }
        }
    }
}