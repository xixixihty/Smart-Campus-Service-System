package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CourseSelectionCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionResponseDTO;
import com.hxq.smart_campus.entity.vo.AvailableCourseVO;
import com.hxq.smart_campus.entity.vo.CourseSelectionListVO;
import com.hxq.smart_campus.entity.vo.MyCourseSelectionVO;

import java.util.List;

public interface CourseSelectionService {
    /**
     * 学生选课
     * @param courseSelectionCreateDTO
     * @return
     */
    CourseSelectionResponseDTO selectCourse(CourseSelectionCreateDTO courseSelectionCreateDTO);

    /**
     * 学生退课
     * @param courseId 课程ID
     * @return
     */
    boolean dropCourse(Long courseId);

    /**
     * 查询学生选课列表
     * @param pageNum
     * @param pageSize
     * @param studentId
     * @param courseId
     * @param semesterId
     * @param status
     * @return
     */
    PageInfo<CourseSelectionListVO> getCourseSelectionList(Integer pageNum, Integer pageSize, Long studentId, Long courseId, Long semesterId, String status);

    /**
     * 查看我的选课课程列表
     * @param semesterId
     * @param status
     * @return
     */
    List<MyCourseSelectionVO> getMyCourseSelectionList(Long semesterId, String status);

    /**
     * 查询可选课程
     * @param semesterId
     * @return
     */
    List<AvailableCourseVO> getAvailableCourseList(Long semesterId);

    /**
     * 清除我的选课缓存（本地Caffeine + Redis）
     * @param studentId 学生ID
     */
    void invalidateMySelectionCache(Long studentId);

    /**
     * 清除可选课程缓存
     * @param studentId 学生ID
     */
    void invalidateAvailableCourseCache(Long studentId);

    /**
     * 查询我的候补课程列表
     * @param studentId 学生ID
     * @param semesterId 学期ID
     * @return
     */
    List<CourseSelectionListVO> getMyWaitingCourses(Long studentId, Long semesterId);

    /**
     * 取消候补
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return
     */
    boolean cancelWaiting(Long studentId, Long courseId);

    /**
     * 查询候补课程数量
     * @param studentId 学生ID
     * @return
     */
    Integer getWaitingCount(Long studentId);
}
