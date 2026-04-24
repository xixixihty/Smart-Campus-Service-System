package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CourseCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.entity.vo.CourseListVO;

import java.util.List;

public interface CourseService {

    /**
     * 获取课程列表
     * @param pageNum
     * @param pageSize
     * @param courseCode
     * @param courseName
     * @param type
     * @param status
     * @return
     */
    PageInfo<CourseListVO> getCourseList(Integer pageNum, Integer pageSize, String courseCode, String courseName, String type, String status);
    /**
     * 获取课程详情
     * @param id
     * @return
     */
    CourseDetailVO getCourseDetail(Long id);

    /**
     * 新增课程
     * @param courseCreateDTO
     * @return
     */
    CourseResponseDTO insertCourse(CourseCreateDTO courseCreateDTO);
    /**
     * 更新课程
     * @param courseUpdateDTO
     * @return
     */
    CourseResponseDTO updateCourse(CourseUpdateDTO courseUpdateDTO);

     /**
     * 删除课程
     * @param ids
     * @return
     */
    boolean deleteCourse(List<Long> ids);
}
