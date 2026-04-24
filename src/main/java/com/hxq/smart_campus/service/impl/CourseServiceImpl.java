package com.hxq.smart_campus.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.constant.MessageConstant;
import com.hxq.smart_campus.entity.dto.CourseCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.entity.vo.CourseListVO;
import com.hxq.smart_campus.mapper.CourseMapper;
import com.hxq.smart_campus.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.hxq.smart_campus.constant.MessageConstant.DATE_TIME_FORMATTER;

@RequiredArgsConstructor
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseMapper courseMapper;

    /**
     * 分页查询课程列表
     */
    @Override
    public PageInfo<CourseListVO> getCourseList(Integer pageNum, Integer pageSize, String courseCode, String courseName, String type, String status) {
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
        return true;
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
}
