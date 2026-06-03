package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseSelectionPeriodUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodDetailVO;
import com.hxq.smart_campus.entity.vo.CourseSelectionPeriodListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface CourseSelectionPeriodMapper {
    /**
     * 创建选课时间段
     * @param courseSelectionPeriodCreateDTO
     * @return
     */
    int insertCourseSelectionPeriod(CourseSelectionPeriodCreateDTO courseSelectionPeriodCreateDTO);
    /**
     * 获取最新插入的ID
     * @return
     */
    @Select("select LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 根据ID获取选课时间段详情
     * @param newCourseSelectionPeriodId
     * @return
     */
    CourseSelectionPeriodDetailVO getCourseSelectionPeriodById(Long newCourseSelectionPeriodId);
    /**
     * 更新选课时间段
     * @param courseSelectionPeriodUpdateDTO
     * @return
     */
    int updateCourseSelectionPeriod(CourseSelectionPeriodUpdateDTO courseSelectionPeriodUpdateDTO);
    /**
     * 获取选课时间段列表
     * @param semesterId
     * @return
     */
    List<CourseSelectionPeriodListVO> getCourseSelectionPeriodList(Long semesterId);

    /**
     * 根据学期ID获取选课时间段列表（一个学期可能有多个选课时间段）
     * @param id 学期ID
     * @return 选课时间段列表
     */
    @Select("select * from course_selection_period where semester_id = #{id}")
    List<CourseSelectionPeriodDetailVO> getCourseSelectionPeriodBySemesterId(Long id);

    /**
     * 批量删除选课时间段
     * @param ids
     * @return
     */
    int deleteBatch(@Param("ids") List<Long> ids);
}
