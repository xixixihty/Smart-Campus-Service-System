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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.spel.ast.Selection;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;
import static com.hxq.smart_campus.constant.RedisConstant.*;
import static com.hxq.smart_campus.utils.TimeConflictUtils.hasTimeConflict;

@Slf4j
@RequiredArgsConstructor
@Service
public class CourseSelectionServiceImpl implements CourseSelectionService {

    private final CourseSelectionMapper courseSelectionMapper;
    private final CourseSelectionPeriodService courseSelectionPeriodService;
    private final CourseService courseService;
    private final SemesterMapper semesterMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;

    private final RedisTemplate redisTemplate;

    private final RedisCourseService redisCourseService;

    private final RedissonClient redissonClient;

    private final CourseCacheService courseCacheService;



    private final String RLOCK_DROP_COURSE_NOT_EXIST = "0";
    private final RabbitTemplate rabbitTemplate;


    /**
     * 学生选课
     * @param courseSelectionCreateDTO
     * @return
     */
    @Override
    public CourseSelectionResponseDTO selectCourse(CourseSelectionCreateDTO courseSelectionCreateDTO) {
//        log.info("学生选课，选课信息：{}", courseSelectionCreateDTO);
//
//        // 判断是否在选课时间段内
//        boolean isInTimePeriod = isInCourseSelectionTimePeriod(courseSelectionCreateDTO.getSemesterId());
//        if (!isInTimePeriod) {
//            throw CourseSelectionException.outOfTimePeriod("不在选课时间段内");
//        }
//        // 判断判断课程是否存在
//        boolean isCourseExist = isCourseExist(courseSelectionCreateDTO.getCourseId());
//        if (!isCourseExist) {
//            throw CourseSelectionException.courseNotExist("课程不存在");
//        }
//        // 判断是否已经选课过该课程
//        boolean isCourseSelectionExist = isCourseSelectionExist(courseSelectionCreateDTO.getCourseId(), courseSelectionCreateDTO.getStudentId());
//        if (isCourseSelectionExist) {
//            throw CourseSelectionException.courseSelectionExist("已经选修过该课程");
//        }
//        // 判断课程的容量是否已满
//        boolean isCourseSelectionFull = isCourseSelectionFull(courseSelectionCreateDTO.getCourseId());
//        if (isCourseSelectionFull) {
//            throw CourseSelectionException.courseSelectionFull("课程已满");
//        }
//        // 该课程可以选课
//        int result = courseSelectionMapper.insertCourseSelection(courseSelectionCreateDTO);
//        if (result <= 0) {
//            throw CourseSelectionException.insertCourseSelectionFailed("选课失败，请稍后再试");
//        }
//        Long courseSelectionId = courseSelectionMapper.getLastInsertId();
//        if (courseSelectionId == null) {
//            throw CourseSelectionException.insertCourseSelectionFailed("选课失败，请稍后再试");
//        }
//        CourseSelectionListVO courseSelectionListVO = courseSelectionMapper.getCourseSelectionListVO(courseSelectionId);
//        if (courseSelectionListVO == null) {
//            throw CourseSelectionException.insertCourseSelectionFailed("选课失败，请稍后再试");
//        }
//        // TODO：插入成功需要减去相应的课程容量
//        courseService.updateCourseCapacity(courseSelectionCreateDTO.getCourseId(), 1);
//        // 先删除课程详情缓存
//        redisTemplate.delete(COURSE_DETAIL_KEY_PREFIX + courseSelectionCreateDTO.getCourseId());
//        // 刷新Redis中的课程列表
//        redisTemplate.opsForValue().set(COURSE_DETAIL_KEY_PREFIX + courseSelectionCreateDTO.getCourseId(), courseSelectionListVO, 1, TimeUnit.DAYS);
// 转换为响应对象        CourseSelectionResponseDTO courseSelectionResponseDTO = convertToCourseSelectionResponseDTO(courseSelectionListVO);
//        courseSelectionResponseDTO.setStudentId(courseSelectionCreateDTO.getStudentId());
//        courseSelectionResponseDTO.setCourseId(courseSelectionCreateDTO.getCourseId());
//        courseSelectionResponseDTO.setSemesterId(courseSelectionCreateDTO.getSemesterId());
//        return courseSelectionResponseDTO;
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
        if (validateTimeConflict(courseSelectionCreateDTO.getCourseId(), courseSelectionCreateDTO.getStudentId(), courseSelectionCreateDTO.getSemesterId())) {
            throw CourseSelectionException.timeConflict("选课时间冲突");
        }

        // 获取分布式锁
        RLock lock = redissonClient.getLock("lock:course:" + courseSelectionCreateDTO.getCourseId());

        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                // Lua脚本执行选课逻辑
                Integer result = redisCourseService.executeSelection(courseSelectionCreateDTO.getCourseId(), courseSelectionCreateDTO.getStudentId());
                // 课程已选
                if (result == -2) {
                    throw CourseSelectionException.courseSelectionExist("已经选修过该课程");
                } else if (result == -1) {
                    // 课程已满，加入候补队列
                    return buildWaitingResponseDTO(courseSelectionCreateDTO);
                } else if (result == 1) {
                    // 选课成功, 异步落库
                    sendSelectionMessage(courseSelectionCreateDTO);
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
        // 根据选课名称获取课程ID
        CourseDetailVO courseDetailVO = courseMapper.getCourseDetailByName(selection.getCourseName());
        if (courseDetailVO == null) {
            throw CourseSelectionException.courseNotExist("课程不存在");
        }
        // 根据学生姓名获取学生ID
        StudentDetailVO studentDetailVO = studentMapper.getStudentDetailByName(selection.getStudentName());
        if (studentDetailVO == null) {
            throw CourseSelectionException.studentNotExist("学生不存在");
        }
        // 获取分布式锁
        RLock lock = redissonClient.getLock("lock:course:" + courseDetailVO.getId());
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                String result = redisCourseService.executeDrop(courseDetailVO.getId(), studentDetailVO.getId());
                if (RLOCK_DROP_COURSE_NOT_EXIST.equals(result)) {
                    throw CourseSelectionException.courseSelectionNotExist("未选择该课程");
                }
                // 更新数据库
                int updateResult = courseSelectionMapper.dropCourse( id);
                if (updateResult <= 0) {
                    throw CourseSelectionException.dropCourseFailed("退课失败，请稍后再试");
                }
                // 如果有候补用户，发送通知
                if (!"1".equals(result)) {
                    sendWaitingNotification(courseDetailVO.getId(), Long.parseLong(result));
                }
                return true;
            } else {
                throw CourseSelectionException.dropCourseFailed("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw CourseSelectionException.dropCourseFailed("退课失败，操作被中断，请稍后再试");
        } finally {
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
        // 从登陆信息中获取到用户信息
        // TODO: 从登录信息中获取用户ID，需要创建SecurityUtils类，用于获取当前登录用户的ID
        Long studentId = SecurityUtils.getCurrentUserId();
        if (studentId == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }
        // 先从本地缓存中获取我的课程列表
        List<MyCourseSelectionVO> localResult = getFromLocalCache(studentId, semesterId, status);
        if (localResult != null && !localResult.isEmpty()) {
            return localResult;
        }
        // 再从Redis中获取我的选课课程列表
        List<MyCourseSelectionVO> myCourseSelectionVORedis = (List<MyCourseSelectionVO>) redisTemplate.opsForValue().get(MY_COURSE_SELECTION_KEY_PREFIX + studentId);
        if (myCourseSelectionVORedis != null && !myCourseSelectionVORedis.isEmpty()) {
            return myCourseSelectionVORedis;
        }
        // 最后从数据库中获取我的选课课程列表
        List<MyCourseSelectionVO> myCourseSelectionList = courseSelectionMapper.getMyCourseSelectionList(studentId, semesterId, status);
        // 将我的课程信息缓存到本地和Redis中
        //         redisTemplate.opsForValue().set(RedisConstant.MY_COURSE_SELECTION_KEY_PREFIX + studentId, myCourseSelectionList, 1, TimeUnit.DAYS);
        //         myCourseSelectionList.stream().forEach(myCourseSelectionVO -> {
        //             redisTemplate.opsForValue().set(RedisConstant.MY_COURSE_SELECTION_KEY_PREFIX + studentId, myCourseSelectionVO, 1, TimeUnit.DAYS);
        //        });
        redisTemplate.opsForValue().set(MY_COURSE_SELECTION_KEY_PREFIX + studentId, myCourseSelectionList, 1, TimeUnit.DAYS);
        putToLocalCache(studentId, semesterId, status, myCourseSelectionList);
        return myCourseSelectionList;
    }

    /**
     * 查询可选课程列表
     * @param semesterId
     * @return
     */
    @Override
    public List<AvailableCourseVO> getAvailableCourseList(Long semesterId) {
        // 判断选课时间是否在范围内
        boolean isCourseSelectionPeriodValid = isInCourseSelectionTimePeriod(semesterId);
        if (!isCourseSelectionPeriodValid) {
            throw CourseSelectionException.courseSelectionTimePeriodInvalid("选课时间不在范围内");
        }
        // 先从Redis中获取可选课程列表
        List<AvailableCourseVO> availableCourseList = (List<AvailableCourseVO>) redisTemplate.opsForValue().get(AVAILABLE_COURSE_KEY_PREFIX + semesterId);
        if (availableCourseList != null) {
            return availableCourseList;
        }
        // 如果Redis中没有，则从数据库中获取
        availableCourseList = courseService.getAvailableCourseList(semesterId);
        if (availableCourseList == null) {
            throw CourseSelectionException.availableCourseListEmpty("可选课程列表为空");
        }
        // 将可选课程信息缓存到Redis中
        //        availableCourseList.stream().forEach(course -> {
        //            redisTemplate.opsForValue().set(AVAILABLE_COURSE_KEY_PREFIX + course.getId(), course, 1, TimeUnit.DAYS);
        //        });
        redisTemplate.opsForValue().set(AVAILABLE_COURSE_KEY_PREFIX + semesterId, availableCourseList, 1, TimeUnit.DAYS);
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
        // 获取课程的上课时间
        CourseDetailVO course = courseMapper.getCourseDetail(courseId);
        if (course == null) {
            return false;
        }
        // 不是可选课程，则返回true
        if (!"ELEXIBLE".equals(course.getScheduleType())) {
            return true;
        }
        // 遍历已选课程
        List<CourseSelectionListVO> selectedCourses = courseSelectionMapper.getCourseSelectionList(studentId, null, semesterId, null);
        for (CourseSelectionListVO selected : selectedCourses) {
            // 获取已选课程的上课时间
            CourseDetailVO selectedCourse = courseMapper.getCourseDetail(selected.getId());
            if (selectedCourse == null) {
                continue;
            }
            if (hasTimeConflict(course, selectedCourse)) {
                throw CourseSelectionException.timeConflict("与课程【" + selected.getCourseName() + "】时间冲突");
            }
        }
            return true;
    }


    private boolean hasTimeConflict(CourseDetailVO course1, CourseDetailVO course2) {
        if (!Objects.equals(course1.getWeekDay(), course2.getWeekDay())) {
            return false;
        }
        if (course1.getClassEnd() <= course2.getClassStart() || course1.getClassStart() >= course2.getClassEnd()) {
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
        if (dto.getSemesterId() == null) {
            throw new IllegalArgumentException("学期ID不能为空");
        }
    }

    /**
     * 发送选课消息
     * @param dto
     */
    private void sendSelectionMessage(CourseSelectionCreateDTO dto) {
        // 发送选课消息
        Map<String, Object> message = new HashMap<>();
        message.put("studentId", dto.getStudentId());
        message.put("courseId", dto.getCourseId());
        message.put("semesterId", dto.getSemesterId());
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
    }
    /**
     *
     */
    private void sendWaitingNotification(Long courseId, Long userId) {
        // TODO: 发送候补成功通知
        log.info("候补用户 {} 成功选课课程 {}", userId, courseId);
    }


    /**
     * 构建选课成功响应DTO
     * @param dto
     * @return
     */
    private CourseSelectionResponseDTO buildSuccessResponseDTO(CourseSelectionCreateDTO dto) {
        CourseDetailVO course = courseCacheService.getCourseDetail(dto.getCourseId());
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
        // TODO: 数据库选课状态字段中没有候补状态，需要根据实际情况修改
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
}


