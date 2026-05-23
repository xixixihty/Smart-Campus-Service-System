package com.hxq.smart_campus.service.mq;

import com.hxq.smart_campus.config.RabbitMQConfig;
import com.hxq.smart_campus.entity.dto.CourseSelectionCreateDTO;
import com.hxq.smart_campus.mapper.CourseSelectionMapper;
import com.hxq.smart_campus.service.CourseSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import java.time.Duration;
import java.util.Map;

import static com.hxq.smart_campus.constant.RedisConstant.MQ_PROCESSED_COURSE_DROP_PREFIX;
import static com.hxq.smart_campus.constant.RedisConstant.MQ_PROCESSED_COURSE_SELECTION_PREFIX;

@Component
@Slf4j
@RequiredArgsConstructor
public class CourseSelectionConsumer {

    private final CourseSelectionMapper courseSelectionMapper;
    private final CourseSelectionService courseSelectionService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration IDEMPOTENT_TTL = Duration.ofMinutes(5);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, containerFactory = "courseSelectionContainerFactory")
    public void handleCourseSelection(Map<String, Object> msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String eventType = (String) msg.getOrDefault("eventType", "SELECT");
            Long studentId = ((Number) msg.get("studentId")).longValue();
            Long courseId = ((Number) msg.get("courseId")).longValue();
            Long semesterId = ((Number) msg.get("semesterId")).longValue();

            log.info("消费选课消息: eventType={}, studentId={}, courseId={}, semesterId={}",
                    eventType, studentId, courseId, semesterId);

            if ("DROP".equals(eventType)) {
                handleDrop(msg, channel, deliveryTag, studentId, courseId, semesterId);
            } else {
                handleSelect(msg, channel, deliveryTag, studentId, courseId, semesterId);
            }
        } catch (Exception e) {
            log.error("消费选课消息失败: msg={}", msg, e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("拒绝选课消息失败", ex);
            }
        }
    }

    private void handleSelect(Map<String, Object> msg, Channel channel, long deliveryTag,
                               Long studentId, Long courseId, Long semesterId) throws Exception {
        String idempotentKey = MQ_PROCESSED_COURSE_SELECTION_PREFIX + studentId + ":" + courseId + ":" + semesterId;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL);
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("选课消息已处理，跳过: studentId={}, courseId={}", studentId, courseId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        int existingCount = courseSelectionMapper.countByStudentAndCourse(studentId, courseId);
        if (existingCount > 0) {
            log.warn("选课记录已存在，跳过插入: studentId={}, courseId={}", studentId, courseId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        var droppedRecord = courseSelectionMapper.selectDroppedRecord(studentId, courseId);
        if (droppedRecord != null) {
            courseSelectionMapper.reactivateCourseSelection(droppedRecord.getId());
            stringRedisTemplate.delete(MQ_PROCESSED_COURSE_DROP_PREFIX + droppedRecord.getId());
            log.info("复用已退课记录，重新激活: studentId={}, courseId={}, recordId={}",
                    studentId, courseId, droppedRecord.getId());
        } else {
            CourseSelectionCreateDTO dto = new CourseSelectionCreateDTO();
            dto.setStudentId(studentId);
            dto.setCourseId(courseId);
            dto.setSemesterId(semesterId);
            courseSelectionMapper.insertCourseSelection(dto);
            log.info("选课记录已写入数据库: studentId={}, courseId={}", studentId, courseId);
        }

        stringRedisTemplate.opsForSet().add("course:selected:" + studentId, String.valueOf(courseId));

        courseSelectionService.invalidateMySelectionCache(studentId);
        courseSelectionService.invalidateAvailableCourseCache(semesterId, studentId);

        channel.basicAck(deliveryTag, false);
    }

    private void handleDrop(Map<String, Object> msg, Channel channel, long deliveryTag,
                             Long studentId, Long courseId, Long semesterId) throws Exception {
        Long id = ((Number) msg.get("id")).longValue();

        String idempotentKey = MQ_PROCESSED_COURSE_DROP_PREFIX + id;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_TTL);
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("退课消息已处理，跳过: id={}, studentId={}, courseId={}", id, studentId, courseId);
            channel.basicAck(deliveryTag, false);
            return;
        }

        courseSelectionMapper.dropCourse(id);
        courseSelectionMapper.dropCourseByStudentAndCourse(studentId, courseId);
        log.info("退课记录已写入数据库: id={}, studentId={}, courseId={}", id, studentId, courseId);

        courseSelectionService.invalidateMySelectionCache(studentId);
        courseSelectionService.invalidateAvailableCourseCache(semesterId, studentId);

        channel.basicAck(deliveryTag, false);
    }
}
