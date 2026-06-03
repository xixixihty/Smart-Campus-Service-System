package com.hxq.smart_campus.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.constant.MessageConstant;
import com.hxq.smart_campus.entity.dto.CourseCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseUpdateDTO;
import com.hxq.smart_campus.entity.vo.AvailableCourseVO;
import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.entity.vo.CourseListVO;
import com.hxq.smart_campus.mapper.CourseMapper;
import com.hxq.smart_campus.mapper.TeacherMapper;
import com.hxq.smart_campus.service.CourseService;
import com.hxq.smart_campus.service.SemesterService;
import com.hxq.smart_campus.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;
import static com.hxq.smart_campus.constant.RedisConstant.AVAILABLE_COURSE_KEY_PREFIX;
import static com.hxq.smart_campus.constant.RedisConstant.COURSE_DETAIL_KEY_PREFIX;

@RequiredArgsConstructor
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;
    private final TeacherMapper teacherMapper;
    private final RedisTemplate redisTemplate;
    private final SemesterService semesterService;

    /**
     * 分页查询课程列表
     */
    @Override
    public PageInfo<CourseListVO> getCourseList(Integer pageNum, Integer pageSize, String courseCode, String courseName, String type, String status) {
        // TODO: 实现Redis缓存课程列表
        // 如果Redis中没有，则从数据库中获取
        PageHelper.startPage(pageNum, pageSize);
        List<CourseListVO> courseList = courseMapper.getCourseList(courseCode, courseName, type, status);
        return new PageInfo<>(courseList);
    }
    /**
     * 获取课程详情
     */
    @Override
    public CourseDetailVO getCourseDetail(Long id) {
        return courseMapper.getCourseDetail(id);
    }
    /**
     * 新增课程
     */
    @Override
    public CourseResponseDTO insertCourse(CourseCreateDTO courseCreateDTO) {
        if (courseCreateDTO == null || courseCreateDTO.getCourseCode() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = courseMapper.insertCourse(courseCreateDTO);
        if (result <= 0) {
            throw new RuntimeException("新增课程失败");
        }
        Long id = courseMapper.getLastInsertId();
        CourseDetailVO courseDetailVO = courseMapper.getCourseDetail(id);
        if (courseDetailVO == null) {
            throw new RuntimeException("查询课程详情失败");
        }
        // 删除Redis缓存
        redisTemplate.delete(COURSE_DETAIL_KEY_PREFIX + id);
        invalidateAvailableCourseCache();
        return convertToResponseDTO(courseDetailVO);
    }
    /**
     * 更新课程
     */
    @Override
    public CourseResponseDTO updateCourse(CourseUpdateDTO courseUpdateDTO) {
        if (courseUpdateDTO == null || courseUpdateDTO.getId() == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int result = courseMapper.updateCourse(courseUpdateDTO);
        if (result <= 0) {
            throw new RuntimeException("更新课程失败");
        }
        CourseDetailVO courseDetailVO = courseMapper.getCourseDetail(courseUpdateDTO.getId());
        if (courseDetailVO == null) {
            throw new RuntimeException("查询课程详情失败");
        }
        // 删除Redis缓存
        redisTemplate.delete(COURSE_DETAIL_KEY_PREFIX + courseUpdateDTO.getId());
        invalidateAvailableCourseCache();
        return convertToResponseDTO(courseDetailVO);
    }
    /**
     * 删除课程
     */
    @Override
    public boolean deleteCourse(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        for (Long id : ids) {
            CourseDetailVO courseDetailVO = courseMapper.getCourseDetail(id);
            if (courseDetailVO == null) {
                throw new IllegalArgumentException("课程不存在");
            }
            if (!MessageConstant.COURSE_STATUS_ENABLED.equals(courseDetailVO.getStatus())) {
                throw new IllegalArgumentException("正开课状态的课程不能删除");
            }
        }
        int result = courseMapper.deleteBatch(ids);
        if (result <= 0){
            throw new RuntimeException("删除课程失败");
        }
        // 删除Redis缓存
        ids.forEach(id -> redisTemplate.delete(COURSE_DETAIL_KEY_PREFIX + id));
        invalidateAvailableCourseCache();
        return true;
    }

    /**
     * 更新课程容量
     * @param courseId
     * @param i
     */
    @Override
    public void updateCourseCapacity(Long courseId, int i) {
        // 查询课程详情
        CourseDetailVO courseDetailVO = courseMapper.getCourseDetail(courseId);
        if (courseDetailVO == null) {
            throw new RuntimeException("课程不存在");
        }
        // 更新课程
        courseMapper.updateCourseCapacity(courseId, i);
        // 删除Redis缓存
        redisTemplate.delete(COURSE_DETAIL_KEY_PREFIX + courseId);
        log.info("更新课程容量成功：{}", LocalDateTime.now().format(DATE_TIME_FORMATTER)); // Log the success message with the current timestamp
    }
    /**
     * 获取可选课程列表
     * @param semesterId
     * @return
     */
    @Override
    public List<AvailableCourseVO> getAvailableCourseList(Long semesterId, Long studentId) {
        // TODO : 根据学期ID获取可选课程列表
        List<AvailableCourseVO> availableCourseVOList = courseMapper.getAvailableCourseList(semesterId, studentId);
        return availableCourseVOList;
    }

    /**
     * 转换课程详情为响应DTO
     */
    public CourseResponseDTO convertToResponseDTO(CourseDetailVO courseDetailVO){
        if (courseDetailVO == null) {
            return null;
        }
        CourseResponseDTO courseResponseDTO = new CourseResponseDTO();
        courseResponseDTO.setId(courseDetailVO.getId());
        courseResponseDTO.setCourseCode(courseDetailVO.getCourseCode());
        courseResponseDTO.setCourseName(courseDetailVO.getCourseName());
        courseResponseDTO.setCredit(courseDetailVO.getCredit());
        courseResponseDTO.setHours(courseDetailVO.getHours());
        courseResponseDTO.setType(courseDetailVO.getType());
        courseResponseDTO.setStatus(courseDetailVO.getStatus());
        courseResponseDTO.setCapacity(courseDetailVO.getCapacity());
        try {
            if (courseDetailVO.getCreateTime() != null && !courseDetailVO.getCreateTime().isEmpty()) {
                courseResponseDTO.setCreateTime(LocalDateTime.parse(courseDetailVO.getCreateTime(), DATE_TIME_FORMATTER));
            }
            if (courseDetailVO.getUpdateTime() != null && !courseDetailVO.getUpdateTime().isEmpty()) {
                courseResponseDTO.setUpdateTime(LocalDateTime.parse(courseDetailVO.getUpdateTime(), DATE_TIME_FORMATTER));
            }
        } catch (Exception e) {
            log.error("转换课程详情为响应DTO失败", e);
        }
        return courseResponseDTO;
    }

    private void invalidateAvailableCourseCache() {
        Set<String> keys = redisTemplate.keys(AVAILABLE_COURSE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已失效可选课程缓存，共 {} 个key", keys.size());
        }
    }

    @Override
    public List<CourseListVO> getTeachingCourses(Long semesterId) {
        Long teacherId = SecurityUtils.getCurrentUserId();
        var currentSemester = semesterService.getCurrentSemester();
        Long currentSemesterId = currentSemester.getId();
        log.info("教师查询所授课程: 前端传入semesterId={}, 强制使用当前学期: {} (ID={})",
                semesterId, currentSemester.getName(), currentSemesterId);
        List<Long> courseIds = teacherMapper.getTeachingCourseIds(teacherId, currentSemesterId);
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return courseMapper.getCoursesByIds(courseIds);
    }

}
