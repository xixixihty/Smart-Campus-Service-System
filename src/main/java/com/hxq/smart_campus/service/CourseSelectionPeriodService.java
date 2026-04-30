package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodDetailVO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodListVO;
import com.hxq.smart_campus.entity.vo.SelectionTimeRedisVO;

import java.util.List;

public interface CourseSelectionPeriodService {
    /**
     * 创建选课时间段
     * @param courseSelectionPeriodCreateDTO
     * @return
     */
    CourseSelectionPeriodResponseDTO insertCourseSelectionPeriod(CourseSelectionPeriodCreateDTO courseSelectionPeriodCreateDTO);
    /**
     * 更新选课时间段
     * @param courseSelectionPeriodUpdateDTO
     * @return
     */
    CourseSelectionPeriodResponseDTO updateCourseSelectionPeriod(CourseSelectionPeriodUpdateDTO courseSelectionPeriodUpdateDTO);

    /**
     * 删除选课时间段
     * @param ids
     * @return
     */
    boolean deleteCourseSelectionPeriod(List<Long> ids);
    /**
     * 获取选课时间段列表
     * @param pageNum
     * @param pageSize
     * @param semesterId
     * @return
     */
    PageInfo<CourseSelectionPeriodListVO> getCourseSelectionPeriodList(Integer pageNum, Integer pageSize, Long semesterId);
    /**
     * 获取选课时间段详情
     * @param id
     * @return
     */
    CourseSelectionPeriodDetailVO getCourseSelectionPeriodDetail(Long id);

    /**
     * 获取当前学期的选课时间
     * @return 选课时间段详情VO
     */
    SelectionTimeRedisVO getCurrentSemesterCourseSelectionPeriod();
}
