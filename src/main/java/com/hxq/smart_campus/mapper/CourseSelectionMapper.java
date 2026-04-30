package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.CourseSelectionCreateDTO;
import com.hxq.smart_campus.entity.vo.CourseSelectionListVO;
import com.hxq.smart_campus.entity.vo.MyCourseSelectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseSelectionMapper {

    /**
     * 查询我的课程选择列表
     * @param studentId
     * @param semesterId
     * @param status
     * @return
     */
    List<MyCourseSelectionVO> getMyCourseSelectionList(@Param("studentId") Long studentId,
                                                       @Param("semesterId") Long semesterId,
                                                       @Param("status") String status);

    /**
     * 学生选课
     * @param courseSelectionCreateDTO
     * @return
     */
    int insertCourseSelection(CourseSelectionCreateDTO courseSelectionCreateDTO);

    /**
     * 获取最新插入的选课ID
     * @return
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 获取单一选课信息
     * @param courseSelectionId
     * @return
     */
    CourseSelectionListVO getCourseSelectionListVO(Long courseSelectionId);

    /**
     * 查询选课列表
     * @param studentId
     * @param courseId
     * @param semesterId
     * @param status
     * @return
     */
    List<CourseSelectionListVO> getCourseSelectionList(@Param("studentId") Long studentId,
                                                       @Param("courseId") Long courseId,
                                                       @Param("semesterId") Long semesterId,
                                                       @Param("status") String status);

    /**
     * 学生退课
     * @param id
     * @return
     */
    int dropCourse(Long id);
}
