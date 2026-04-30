package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.CourseCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseUpdateDTO;
import com.hxq.smart_campus.entity.vo.AvailableCourseVO;
import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.entity.vo.CourseListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CourseMapper {
    /**
     * 获取课程列表
     * @param courseCode
     * @param courseName
     * @param type
     * @param status
     * @return
     */
    List<CourseListVO> getCourseList(@Param("courseCode") String courseCode,
                                     @Param("courseName") String courseName,
                                     @Param("type") String type,
                                     @Param("status") String status);

    /**
     * 获取课程详情
     * @param id
     * @return
     */
    @Select("select id, course_code, course_name, credit, hours, type, status, capacity, create_time, update_time from course where id = #{id}")
    CourseDetailVO getCourseDetail(Long id);
    /**
     * 新增课程
     * @param courseCreateDTO
     * @return
     */
    int insertCourse(CourseCreateDTO courseCreateDTO);
    /**
     * 获取最新插入的课程ID
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();
    /**
     * 更新课程
     * @param courseUpdateDTO
     * @return
     */
    int updateCourse(CourseUpdateDTO courseUpdateDTO);
    /**
     * 批量删除课程
     * @param ids
     * @return
     */
    int deleteBatch(@Param("ids") List<Long> ids);

    /**
     * 更新课程容量
     * @param courseId
     * @param i 增加或减少的容量值
     */
    @Update("update course set capacity = capacity - #{i} where id = #{courseId}")
    void updateCourseCapacity(Long courseId, int i);

    /**
     * 获取可选课程列表
     * @param semesterId
     * @return
     */
    List<AvailableCourseVO> getAvailableCourseList(Long semesterId);

    /**
     * 查询所有课程ID
     * @return
     */
    @Select("select id from course")
    List<Long> selectAllCourseIds();

    /**
     * 根据课程名称查询课程详情
     * @param courseName
     * @return
     */
    @Select("select id, course_code, course_name, credit, hours, type, status, capacity, create_time, update_time from course where course_name = #{courseName}")
    CourseDetailVO getCourseDetailByName(String courseName);
}
