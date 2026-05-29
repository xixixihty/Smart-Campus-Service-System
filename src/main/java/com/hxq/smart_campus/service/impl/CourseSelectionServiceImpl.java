package com.hxq.smart_campus.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.config.RabbitMQConfig;
import com.hxq.smart_campus.constant.RedisConstant;
import com.hxq.smart_campus.entity.dto.CourseSelectionCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionResponseDTO;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.exception.CourseSelectionException;
import com.hxq.smart_campus.mapper.*;
import com.hxq.smart_campus.service.CourseSelectionPeriodService;
import com.hxq.smart_campus.service.CourseSelectionService;
import com.hxq.smart_campus.service.CourseService;
import com.hxq.smart_campus.service.SemesterService;
import com.hxq.smart_campus.utils.SecurityUtils;
import com.hxq.smart_campus.utils.TimeConflictUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;
import static com.hxq.smart_campus.constant.RedisConstant.*;

@Slf4j
@Service
public class CourseSelectionServiceImpl implements CourseSelectionService {

    private final CourseSelectionMapper courseSelectionMapper;
    private final CourseSelectionPeriodService courseSelectionPeriodService;
    private final CourseService courseService;
    private final SemesterMapper semesterMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;

    private final RedisTemplate redisTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisCourseService redisCourseService;

    private final RedissonClient redissonClient;

    private final CourseCacheService courseCacheService;

    private final RabbitTemplate rabbitTemplate;

    public CourseSelectionServiceImpl(
            CourseSelectionMapper courseSelectionMapper,
            CourseSelectionPeriodService courseSelectionPeriodService,
            CourseService courseService,
            SemesterMapper semesterMapper,
            CourseMapper courseMapper,
            StudentMapper studentMapper,
            RedisTemplate redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            RedisCourseService redisCourseService,
            RedissonClient redissonClient,
            CourseCacheService courseCacheService,
            @Qualifier("borrowRabbitTemplate") RabbitTemplate rabbitTemplate) {
        this.courseSelectionMapper = courseSelectionMapper;
        this.courseSelectionPeriodService = courseSelectionPeriodService;
        this.courseService = courseService;
        this.semesterMapper = semesterMapper;
        this.courseMapper = courseMapper;
        this.studentMapper = studentMapper;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisCourseService = redisCourseService;
        this.redissonClient = redissonClient;
        this.courseCacheService = courseCacheService;
        this.rabbitTemplate = rabbitTemplate;
    }

    private final String RLOCK_DROP_COURSE_NOT_EXIST = "0";

    private static final String AVAILABLE_SUPPRESS_PREFIX = "available:suppress:";


    /**
     * 学生选课
     * @param courseSelectionCreateDTO
     * @return
     */
    @Override
    public CourseSelectionResponseDTO selectCourse(CourseSelectionCreateDTO courseSelectionCreateDTO) {
        // 判断是否在选课时间段内
        /**
         * 1. 判断是否在选课时间段内
         */
        boolean isInTimePeriod = isInCourseSelectionTimePeriod(courseSelectionCreateDTO.getSemesterId());
        if (!isInTimePeriod) {
            throw CourseSelectionException.outOfTimePeriod("不在选课时间段内");
        }
        log.info("学生选课，参数：{}", courseSelectionCreateDTO);
        validateSelectionDTO(courseSelectionCreateDTO);
        /**
         * 2. 判断课程是否存在
         */
        if (!redisCourseService.checkCourseExists(courseSelectionCreateDTO.getCourseId())) {
            throw CourseSelectionException.courseNotExist("课程不存在");
        }
        /**
         * 3. 判断选课时间冲突
         */
        if (validateTimeConflict(courseSelectionCreateDTO.getStudentId(), courseSelectionCreateDTO.getCourseId(), courseSelectionCreateDTO.getSemesterId())) {
            throw CourseSelectionException.timeConflict("选课时间冲突");
        }

        // 获取分布式锁
        RLock lock = redissonClient.getLock(LOCK_COURSE_KEY_PREFIX + courseSelectionCreateDTO.getCourseId());

        try {
            // 尝试获取锁，超时时间3秒，等待时间10秒
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                var droppedRecord = courseSelectionMapper.selectDroppedRecord(
                        courseSelectionCreateDTO.getStudentId(), courseSelectionCreateDTO.getCourseId());
                if (droppedRecord != null) {
                    String selectedKey = "course:selected:" + courseSelectionCreateDTO.getStudentId();
                    String courseIdStr = String.valueOf(courseSelectionCreateDTO.getCourseId());
                    Boolean alreadySelected = stringRedisTemplate.opsForSet().isMember(selectedKey, courseIdStr);
                    if (Boolean.TRUE.equals(alreadySelected)) {
                        throw CourseSelectionException.courseSelectionExist("已经选修过该课程");
                    }
                    stringRedisTemplate.opsForSet().remove(selectedKey, courseIdStr);
                    log.info("检测到已退课记录，已清理Redis选课集合: studentId={}, courseId={}",
                            courseSelectionCreateDTO.getStudentId(), courseSelectionCreateDTO.getCourseId());
                    String idempotentKey = MQ_PROCESSED_COURSE_SELECTION_PREFIX
                            + courseSelectionCreateDTO.getStudentId() + ":"
                            + courseSelectionCreateDTO.getCourseId() + ":"
                            + courseSelectionCreateDTO.getSemesterId();
                    stringRedisTemplate.delete(idempotentKey);
                    String dropIdempotentKey = MQ_PROCESSED_COURSE_DROP_PREFIX + droppedRecord.getId();
                    stringRedisTemplate.delete(dropIdempotentKey);
                }

                Long result = redisCourseService.executeSelection(courseSelectionCreateDTO.getCourseId(), courseSelectionCreateDTO.getStudentId());
                log.info("选课Lua脚本执行结果: result={}, courseId={}, studentId={}", result,
                        courseSelectionCreateDTO.getCourseId(), courseSelectionCreateDTO.getStudentId());
                if (result == -2) {
                    throw CourseSelectionException.courseSelectionExist("已经选修过该课程");
                } else if (result == -3) {
                    throw CourseSelectionException.alreadyInWaiting("已在候补队列中");
                } else if (result == -4) {
                    throw CourseSelectionException.waitingLimitExceeded("候补课程已达上限(3门)");
                } else if (result == -1) {
                    sendWaitingMessage(courseSelectionCreateDTO);
                    return buildWaitingResponseDTO(courseSelectionCreateDTO);
                } else if (result == 1) {
                    sendSelectionMessage(courseSelectionCreateDTO);
                    invalidateMySelectionCache(courseSelectionCreateDTO.getStudentId());
                    invalidateAvailableCourseCache(courseSelectionCreateDTO.getStudentId());
                    stringRedisTemplate.opsForValue().set(
                            AVAILABLE_SUPPRESS_PREFIX + courseSelectionCreateDTO.getStudentId(),
                            "1", Duration.ofSeconds(10));
                    return buildSuccessResponseDTO(courseSelectionCreateDTO);
                }
                throw CourseSelectionException.insertCourseSelectionFailed("选课失败，请稍后再试");
            }
            else {
                throw CourseSelectionException.insertCourseSelectionFailed("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CourseSelectionException.insertCourseSelectionFailed("选课失败，操作被中断，请稍后再试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
    /**
     * 学生退课
     * @param id
     * @return
     */
    @Override
    public boolean dropCourse(Long id) {

//        // TODO: 需要从登陆信息中获取用户ID
//        Long studentId = SecurityUtils.getCurrentUserId();
//        // TODO: 需要从登陆信息中获取用户姓名
//        String studentName = SecurityUtils.getCurrentUserName();
//        // 根据学生ID获取到学生的个人选课信息
//        CourseSelectionListVO courseSelectionListVO = courseSelectionMapper.getCourseSelectionListVO(id);
//        // 根据学期名称获取到学期详情
//        SemesterDetailVO semesterDetailVO = semesterMapper.getSemesterDetailByName(courseSelectionListVO.getSemesterName());
//        if (semesterDetailVO == null) {
//            throw new IllegalArgumentException("学期不存在");
//        }
//        // 判断课程是否存在
//        if (courseSelectionListVO == null) {
//            throw CourseSelectionException.courseSelectionNotExist("课程不存在");
//        }
//        // 判断是否是该学生的选课
//        if (!courseSelectionListVO.getStudentName().equals(studentName)) {
//            throw CourseSelectionException.courseSelectionNotExist("该课程不是该学生的选课");
//        }
//        // 先删除学生个人选课信息
//        redisTemplate.delete(RedisConstant.MY_COURSE_SELECTION_KEY_PREFIX + studentId);
//        // 再更新数据库
//        int result = courseSelectionMapper.dropCourse(id);
//        if (result <= 0) {
//            throw CourseSelectionException.dropCourseFailed("退课失败，请稍后再试");
//        }
//        // 再查询我的选课课程列表
//        List<CourseSelectionListVO> courseSelectionList = courseSelectionMapper.getCourseSelectionList(studentId, null, semesterDetailVO.getId(), courseSelectionListVO.getStatus());
//        // 刷新Redis中的我的选课课程列表
//        redisTemplate.opsForValue().set(RedisConstant.MY_COURSE_SELECTION_KEY_PREFIX + studentId, courseSelectionList, 1, TimeUnit.DAYS);
//        // 返回数据
//        return true;
        log.info("学生退课，参数：{}", id);
        CourseSelectionListVO selection = courseSelectionMapper.getCourseSelectionListVO(id);
        if (selection == null) {
            throw CourseSelectionException.courseSelectionNotExist("课程不存在");
        }
        RLock lock = redissonClient.getLock(LOCK_COURSE_KEY_PREFIX + selection.getCourseId());
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                String result = redisCourseService.executeDrop(selection.getCourseId(), selection.getStudentId());
                if (RLOCK_DROP_COURSE_NOT_EXIST.equals(result)) {
                    if (!"已选".equals(selection.getStatus())) {
                        throw CourseSelectionException.courseSelectionNotExist("未选择该课程");
                    }
                    log.warn("Redis选课集合不一致，修复中: studentId={}, courseId={}",
                            selection.getStudentId(), selection.getCourseId());
                    stringRedisTemplate.opsForSet().add(
                            "course:selected:" + selection.getStudentId(),
                            String.valueOf(selection.getCourseId()));
                    result = redisCourseService.executeDrop(selection.getCourseId(), selection.getStudentId());
                    if (RLOCK_DROP_COURSE_NOT_EXIST.equals(result)) {
                        throw CourseSelectionException.courseSelectionNotExist("Redis修复后仍无法退课，请联系管理员");
                    }
                }
                // 发送MQ消息异步落库
                sendDropMessage(id, selection.getStudentId(), selection.getCourseId(), selection.getSemesterId());
                // 如果有候补用户，发送通知
                if (!"1".equals(result)) {
                    sendWaitingNotification(selection.getCourseId(), Long.parseLong(result));
                }
                return true;
            } else {
                throw CourseSelectionException.dropCourseFailed("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CourseSelectionException.dropCourseFailed("退课失败，操作被中断，请稍后再试");
        } finally {
            if (selection != null) {
                invalidateMySelectionCache(selection.getStudentId());
                invalidateAvailableCourseCache(selection.getStudentId());
            }
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
}

    /**
     * 获取课程选择列表
     * @param pageNum
     * @param pageSize
     * @param studentId
     * @param courseId
     * @param semesterId
     * @param status
     * @return
     */
    @Override
    public PageInfo<CourseSelectionListVO> getCourseSelectionList(Integer pageNum, Integer pageSize, Long studentId, Long courseId, Long semesterId, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<CourseSelectionListVO> courseSelectionList = courseSelectionMapper.getCourseSelectionList(studentId, courseId, semesterId, status);
        return new PageInfo<>(courseSelectionList);
    }

    /**
     * 查询我的选课课程列表
     * @param semesterId
     * @param status
     * @return
     */
    @Override
    public List<MyCourseSelectionVO> getMyCourseSelectionList(Long semesterId, String status) {
        Long studentId = SecurityUtils.getCurrentUserId();
        if (studentId == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }
        if (status == null || status.isEmpty()) {
            status = "已选";
        }
        List<MyCourseSelectionVO> localResult = getFromLocalCache(studentId, semesterId, status);
        if (localResult != null && !localResult.isEmpty()) {
            return localResult;
        }
        String redisKey = MY_COURSE_SELECTION_KEY_PREFIX + studentId + ":" + semesterId + ":" + status;
        List<MyCourseSelectionVO> myCourseSelectionVORedis = (List<MyCourseSelectionVO>) redisTemplate.opsForValue().get(redisKey);
        if (myCourseSelectionVORedis != null && !myCourseSelectionVORedis.isEmpty()) {
            return myCourseSelectionVORedis;
        }
        List<MyCourseSelectionVO> myCourseSelectionList = courseSelectionMapper.getMyCourseSelectionList(studentId, semesterId, status);
        if (myCourseSelectionList != null && !myCourseSelectionList.isEmpty()) {
            redisTemplate.opsForValue().set(redisKey, myCourseSelectionList, 1, TimeUnit.DAYS);
            putToLocalCache(studentId, semesterId, status, myCourseSelectionList);
        }
        return myCourseSelectionList;
    }

    /**
     * 查询可选课程列表
     * @param semesterId
     * @return
     */
    @Override
    public List<AvailableCourseVO> getAvailableCourseList(Long semesterId) {
        boolean isCourseSelectionPeriodValid = isInCourseSelectionTimePeriod(semesterId);
        if (!isCourseSelectionPeriodValid) {
            throw CourseSelectionException.courseSelectionTimePeriodInvalid("选课时间不在范围内");
        }
        Long studentId = SecurityUtils.getCurrentUserId();
        String cacheKey = AVAILABLE_COURSE_KEY_PREFIX + semesterId + ":" + studentId;
        List<AvailableCourseVO> availableCourseList = (List<AvailableCourseVO>) redisTemplate.opsForValue().get(cacheKey);
        if (availableCourseList != null && !availableCourseList.isEmpty()) {
            return availableCourseList;
        }
        availableCourseList = courseService.getAvailableCourseList(semesterId, studentId);
        if (availableCourseList == null || availableCourseList.isEmpty()) {
            return List.of();
        }
        Boolean suppressed = stringRedisTemplate.hasKey(AVAILABLE_SUPPRESS_PREFIX + studentId);
        if (Boolean.TRUE.equals(suppressed)) {
            redisTemplate.opsForValue().set(cacheKey, availableCourseList, 5, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(cacheKey, availableCourseList, 30, TimeUnit.MINUTES);
        }
        return availableCourseList;
    }



    /**
     * 将课程选择列表转换为课程选择响应DTO
     * @param courseSelectionListVO
     * @return
     */
    private CourseSelectionResponseDTO convertToCourseSelectionResponseDTO(CourseSelectionListVO courseSelectionListVO) {
        if (courseSelectionListVO == null) {
            throw new IllegalArgumentException("课程选择列表不能为空");
        }
        CourseSelectionResponseDTO courseSelectionResponseDTO = new CourseSelectionResponseDTO();
        courseSelectionResponseDTO.setId(courseSelectionListVO.getId());
        // courseSelectionResponseDTO.setStudentId(courseSelectionListVO.getStudentId());
        courseSelectionResponseDTO.setStudentName(courseSelectionListVO.getStudentName());
        // courseSelectionResponseDTO.setCourseId(courseSelectionListVO.getCourseId());
        courseSelectionResponseDTO.setCourseName(courseSelectionListVO.getCourseName());
        courseSelectionResponseDTO.setCredit(courseSelectionListVO.getCredit());
        // courseSelectionResponseDTO.setSemesterId(courseSelectionListVO.getSemesterId());
        courseSelectionResponseDTO.setSemesterName(courseSelectionListVO.getSemesterName());
        courseSelectionResponseDTO.setStatus(courseSelectionListVO.getStatus());
        courseSelectionResponseDTO.setScore(courseSelectionListVO.getScore());
        courseSelectionResponseDTO.setScorePoint(courseSelectionListVO.getScorePoint());
        try {
            if (courseSelectionListVO.getCreateTime() != null && !courseSelectionListVO.getCreateTime().isEmpty()) {
                courseSelectionResponseDTO.setCreateTime(LocalDateTime.parse(courseSelectionListVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (courseSelectionListVO.getUpdateTime() != null && !courseSelectionListVO.getUpdateTime().isEmpty()) {
                courseSelectionResponseDTO.setUpdateTime(LocalDateTime.parse(courseSelectionListVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("时间转换错误", e);
        }
        return courseSelectionResponseDTO;
    }
    /**
     * 判断是否在选课时间段内
     * @param semesterId
     * @return
     */
    private Boolean isInCourseSelectionTimePeriod(Long semesterId) {
        // 先从缓存中获取选课时间段
        SelectionTimeRedisVO selectionTimeRedisVO = (SelectionTimeRedisVO) redisTemplate.opsForValue().get(COURSE_SELECTION_KEY_PERIOD + semesterId);
        if (selectionTimeRedisVO != null) {
            return selectionTimeRedisVO.getStartTime().isBefore(LocalDateTime.now()) && selectionTimeRedisVO.getEndTime().isAfter(LocalDateTime.now());
        }
        // 缓存中没有，则从数据库中获取
        // 直接调用课程时间段服务
        SelectionTimeRedisVO timeRedisVO = courseSelectionPeriodService.getCurrentSemesterCourseSelectionPeriod();
        // 再将查询到的数据加入到Redis中
        redisTemplate.opsForValue().set(COURSE_SELECTION_KEY_PERIOD + semesterId, timeRedisVO, 1, TimeUnit.DAYS);
        return timeRedisVO.getStartTime().isBefore(LocalDateTime.now()) && timeRedisVO.getEndTime().isAfter(LocalDateTime.now());
    }
    /**
     * 判断课程是否存在
     */
    private Boolean isCourseExist(Long courseId) {
        // 从Redis中获取课程信息
        CourseDetailVO courseDetailVORedis = (CourseDetailVO) redisTemplate.opsForValue().get(COURSE_DETAIL_KEY_PREFIX + courseId);
        if (courseDetailVORedis != null) {
            return true;
        }
        // Redis中没有，则从数据库中获取
        CourseDetailVO courseDetailVO = courseService.getCourseDetail(courseId);
        if (courseDetailVO != null) {
            // 将查询到的数据加入Redis缓存中
            redisTemplate.opsForValue().set(COURSE_DETAIL_KEY_PREFIX + courseId, courseDetailVO, 1, TimeUnit.DAYS);
            return true;
        }
        return false;
    }

    /**
     * 判断学生是否已经选修了该课程
     * @param courseId
     * @param studentId
     * @return
     */
    private Boolean isCourseSelectionExist(Long courseId, Long studentId) {
        // 根据课程ID查询课程名称
        String courseName = courseService.getCourseDetail(courseId).getCourseName();
        // 从Redis中获取学生的选课课程列表
        List<MyCourseSelectionVO> myCourseSelectionListRedis = (List<MyCourseSelectionVO>) redisTemplate.opsForValue().get(RedisConstant.MY_COURSE_SELECTION_KEY_PREFIX + studentId);
        if (myCourseSelectionListRedis != null) {
            // 获取课程列表中的课程名称
            List<String> courseNameList = myCourseSelectionListRedis.stream()
                    .map(MyCourseSelectionVO::getCourseName)
                    .collect(Collectors.toList());
            // 判断课程名称是否在列表中
            return courseNameList.contains(courseName);
        }
        return false;
    }

    /**
     * 判断课程是否已满
     * @param courseId
     * @return
     */
    private Boolean isCourseSelectionFull(Long courseId) {
        // 先判断课程是否存在
        if (!isCourseExist(courseId)) {
            return false;
        }
        // 先判断课程容量是否为0
        CourseDetailVO courseDetailVO = courseService.getCourseDetail(courseId);
        if (courseDetailVO.getCapacity() == null) {
            return true;
        }
        // 从Redis中获取课程信息
        CourseDetailVO courseDetailVORedis = (CourseDetailVO) redisTemplate.opsForValue().get(COURSE_DETAIL_KEY_PREFIX + courseId);
        if (courseDetailVORedis != null) {
            // 从Redis中获取课程选课人数
            Short capacity = courseDetailVORedis.getCapacity();
            // 判断课程容量是否已满
            if (capacity != null && capacity >= courseDetailVO.getCapacity()) {
                // 课程容量已满
                return true;
            }
        }
        return false;
    }


    /**
     * 验证时间冲突
     * @param studentId
     * @param courseId
     * @param semesterId
     * @return
     */
    private Boolean validateTimeConflict(Long studentId, Long courseId, Long semesterId) {
        CourseDetailVO course = courseMapper.getCourseDetail(courseId);
        if (course == null) {
            return false;
        }
        if ("灵活".equals(course.getScheduleType())) {
            return false;
        }
        List<CourseSelectionListVO> selectedCourses = courseSelectionMapper.getCourseSelectionList(studentId, null, semesterId, null);
        for (CourseSelectionListVO selected : selectedCourses) {
            CourseDetailVO selectedCourse = courseMapper.getCourseDetail(selected.getCourseId());
            if (selectedCourse == null) {
                continue;
            }
            if (hasTimeConflict(course, selectedCourse)) {
                return true;
            }
        }
        return false;
    }


    private boolean hasTimeConflict(CourseDetailVO course1, CourseDetailVO course2) {
        if (!Objects.equals(course1.getWeekDay(), course2.getWeekDay())) {
            return false;
        }
        // 节次冲突检测：若任一值为null则无法判定冲突
        if (course1.getClassEnd() == null || course2.getClassStart() == null ||
            course1.getClassStart() == null || course2.getClassEnd() == null) {
            return false;
        }
        if (course1.getClassEnd() <= course2.getClassStart() || course1.getClassStart() >= course2.getClassEnd()) {
            return false;
        }
        // 周次冲突检测：若任一值为null则无法判定冲突
        if (course1.getWeekEnd() == null || course2.getWeekStart() == null ||
            course1.getWeekStart() == null || course2.getWeekEnd() == null) {
            return false;
        }
        if (course1.getWeekEnd() <= course2.getWeekStart() || course1.getWeekStart() >= course2.getWeekEnd()) {
          return false;
        }
        return true;
    }

    /**
     * 验证选课DTO
     * @param dto
     */
    private void validateSelectionDTO(CourseSelectionCreateDTO dto) {
        if (dto.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (dto.getCourseId() == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        // 兜底：如果Controller未填充semesterId，则自动查询当前学期
        if (dto.getSemesterId() == null) {
            var currentSemester = semesterMapper.getCurrentSemester();
            if (currentSemester == null) {
                throw new IllegalArgumentException("当前学期未设置，请联系管理员");
            }
            dto.setSemesterId(currentSemester.getId());
            log.info("Service层兜底：自动填充当前学期ID：{}", currentSemester.getId());
        }
    }

    private void sendWaitingMessage(CourseSelectionCreateDTO dto) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", "WAITING");
        message.put("studentId", dto.getStudentId());
        message.put("courseId", dto.getCourseId());
        message.put("semesterId", dto.getSemesterId());
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
        log.info("发送候补消息: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId());
    }

    /**
     * 发送选课消息
     * @param dto
     */
    private void sendSelectionMessage(CourseSelectionCreateDTO dto) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", "SELECT");
        message.put("studentId", dto.getStudentId());
        message.put("courseId", dto.getCourseId());
        message.put("semesterId", dto.getSemesterId());
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }

    /**
     * 发送退课消息
     * @param id 选课记录ID
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param semesterId 学期ID
     */
    private void sendDropMessage(Long id, Long studentId, Long courseId, Long semesterId) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", "DROP");
        message.put("id", id);
        message.put("studentId", studentId);
        message.put("courseId", courseId);
        message.put("semesterId", semesterId);
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
        log.info("发送退课消息: id={}, studentId={}, courseId={}", id, studentId, courseId);
    }
    /**
     *
     */
    private void sendWaitingNotification(Long courseId, Long userId) {
        log.info("候补用户 {} 成功选课课程 {}", userId, courseId);
        Long semesterId = semesterMapper.getCurrentSemester().getId();
        CourseSelectionCreateDTO dto = new CourseSelectionCreateDTO();
        dto.setStudentId(userId);
        dto.setCourseId(courseId);
        dto.setSemesterId(semesterId);
        sendSelectionMessage(dto);
        invalidateMySelectionCache(userId);
        invalidateAvailableCourseCache(userId);
    }


    /**
     * 构建选课成功响应DTO
     * @param dto
     * @return
     */
    private CourseSelectionResponseDTO buildSuccessResponseDTO(CourseSelectionCreateDTO dto) {
        CourseDetailVO course = courseCacheService.getCourseDetail(dto.getCourseId());
        if (course == null) {
            log.error("构建选课响应时课程信息为空: courseId={}", dto.getCourseId());
            course = courseService.getCourseDetail(dto.getCourseId());
            if (course == null) {
                throw CourseSelectionException.courseNotExist("课程信息丢失，请联系管理员");
            }
        }
        CourseSelectionResponseDTO responseDTO = new CourseSelectionResponseDTO();
        responseDTO.setStudentId(dto.getStudentId());
        responseDTO.setCourseId(dto.getCourseId());
        responseDTO.setCourseName(course.getCourseName());
        responseDTO.setSemesterId(dto.getSemesterId());
        responseDTO.setStatus("已选");
        responseDTO.setCreateTime(LocalDateTime.now());
        return responseDTO;
    }

    /**
     * 构建候补选课响应DTO
     * @param dto
     * @return
     */
    private CourseSelectionResponseDTO buildWaitingResponseDTO(CourseSelectionCreateDTO dto) {
        CourseSelectionResponseDTO response = new CourseSelectionResponseDTO();
        response.setStudentId(dto.getStudentId());
        response.setCourseId(dto.getCourseId());
        response.setSemesterId(dto.getSemesterId());
        response.setStatus("候补");
        response.setCreateTime(LocalDateTime.now());
        return response;
    }

    /**
     * 本地缓存相关方法
     */
    private final Cache<String, List<MyCourseSelectionVO>> mySelectionCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * 从本地缓存中获取学生选课列表
     * @param studentId
     * @param semesterId
     * @param status
     * @return
     */
    private List<MyCourseSelectionVO> getFromLocalCache(Long studentId, Long semesterId, String status) {
        String key = buildCacheKey(studentId, semesterId, status);
        return mySelectionCache.getIfPresent(key);
    }

    /**
     * 向本地缓存中写入学生选课列表
     * @param studentId
     */
    private void putToLocalCache(Long studentId, Long semesterId, String status, List<MyCourseSelectionVO> value) {
        String key = buildCacheKey(studentId, semesterId, status);
        mySelectionCache.put(key, value);
    }

    /**
     * 构建缓存键
     * @param studentId
     * @param semesterId
     * @param status
     * @return
     */
    private String buildCacheKey(Long studentId, Long semesterId, String status) {
        return "my:selection:" + studentId + ":" + semesterId + ":" + status;
    }

    @Override
    public void invalidateMySelectionCache(Long studentId) {
        String prefix = "my:selection:" + studentId + ":";
        java.util.Set<String> keysToInvalidate = mySelectionCache.asMap().keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .collect(java.util.stream.Collectors.toSet());
        keysToInvalidate.forEach(mySelectionCache::invalidate);

        Set<String> keys = redisTemplate.keys(MY_COURSE_SELECTION_KEY_PREFIX + studentId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        log.info("已失效学生选课缓存: studentId={}, caffeineKeys={}, redisKeys={}",
                studentId, keysToInvalidate.size(), keys != null ? keys.size() : 0);
    }

    @Override
    public void invalidateAvailableCourseCache(Long studentId) {
        Set<String> keys = redisTemplate.keys(AVAILABLE_COURSE_KEY_PREFIX + "*:" + studentId);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已失效可选课程缓存: studentId={}, keys={}", studentId, keys.size());
        }
    }

    @Override
    public List<CourseSelectionListVO> getMyWaitingCourses(Long studentId, Long semesterId) {
        return courseSelectionMapper.getCourseSelectionList(studentId, null, semesterId, "候补");
    }

    @Override
    public boolean cancelWaiting(Long studentId, Long courseId) {
        String waitingKey = "course:waiting:" + courseId;
        String waitingCountKey = "course:waiting:count:" + studentId;
        Long removed = stringRedisTemplate.opsForZSet().remove(waitingKey, String.valueOf(studentId));
        if (removed == null || removed == 0) {
            return false;
        }
        stringRedisTemplate.opsForValue().decrement(waitingCountKey);
        courseSelectionMapper.cancelWaiting(studentId, courseId);
        invalidateMySelectionCache(studentId);
        log.info("取消候补成功: studentId={}, courseId={}", studentId, courseId);
        return true;
    }

    @Override
    public Integer getWaitingCount(Long studentId) {
        String waitingCountKey = "course:waiting:count:" + studentId;
        String count = stringRedisTemplate.opsForValue().get(waitingCountKey);
        return count != null ? Integer.parseInt(count) : 0;
    }
}


