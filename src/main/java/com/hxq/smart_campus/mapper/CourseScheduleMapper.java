package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.CourseScheduleCreateDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleResponseDTO;
import com.hxq.smart_campus.entity.dto.CourseScheduleUpdateDTO;
import com.hxq.smart_campus.entity.vo.CourseScheduleDetailVO;
import com.hxq.smart_campus.entity.vo.CourseScheduleListVO;
import com.hxq.smart_campus.entity.vo.StudentCourseVO;
import com.hxq.smart_campus.entity.vo.TimetableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CourseScheduleMapper {
    /**
     * 创建排课
     * @param courseScheduleCreateDTO
     * @return
     */
    int insertCourseSchedule(CourseScheduleCreateDTO courseScheduleCreateDTO);
    /**
     * 获取最新插入的排课ID
     * @return
     */
    @Select("select last_insert_id()")
    Long getLastInsertId();
    /**
     * 根据排课ID查询排课详情
     * @param id
     * @return
     */
    CourseScheduleDetailVO getCourseScheduleDetail(Long id);

    /**
     * 更新排课
     * @param courseScheduleUpdateDTO
     * @return
     */
    int updateCourseSchedule(CourseScheduleUpdateDTO courseScheduleUpdateDTO);
    /**
     * 批量删除排课
     * @param ids
     * @return
     */
    int deleteCourseScheduleBatch(@Param("ids") List<Long> ids);
    /**
     * 获取排课列表
     * @param semesterId 学期ID
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param classroomId 教室ID
     * @param weekDay 星期几
     * @return
     */
    List<CourseScheduleListVO> getCourseScheduleList(@Param("semesterId") Long semesterId,
                                                      @Param("courseId") Long courseId,
                                                      @Param("teacherId") Long teacherId,
                                                      @Param("classroomId") Long classroomId,
                                                      @Param("weekDay") Integer weekDay,
                                                      @Param("weekNum") Integer weekNum);
    /**
     * 查询课表
     * @param semesterId 学期ID
     * @param userId 用户ID
     * @param userType 用户类型
     * @param classIds 班级ID列表（学生查询时使用）
     * @return
     */
    List<TimetableVO> queryTimetable(@Param("semesterId") Long semesterId,
                                     @Param("userId") Long userId,
                                     @Param("userType") String userType,
                                     @Param("classIds") List<Long> classIds);

    /**
     * 检测教师时间冲突
     * @param semesterId 学期ID
     * @param teacherId 教师ID
     * @param weekDay 星期几
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @param weekRange 周次范围
     * @param excludeId 排除的排课ID（更新时使用）
     * @return
     */
    List<CourseScheduleDetailVO> checkTeacherConflict(@Param("semesterId") Long semesterId,
                                                      @Param("teacherId") Long teacherId,
                                                      @Param("weekDay") Integer weekDay,
                                                      @Param("startSection") Integer startSection,
                                                      @Param("endSection") Integer endSection,
                                                      @Param("weekRange") String weekRange,
                                                      @Param("excludeId") Long excludeId);

    /**
     * 检测教室时间冲突
     * @param semesterId 学期ID
     * @param classroomId 教室ID
     * @param weekDay 星期几
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @param weekRange 周次范围
     * @param excludeId 排除的排课ID（更新时使用）
     * @return
     */
    List<CourseScheduleDetailVO> checkClassroomConflict(@Param("semesterId") Long semesterId,
                                                         @Param("classroomId") Long classroomId,
                                                         @Param("weekDay") Integer weekDay,
                                                         @Param("startSection") Integer startSection,
                                                         @Param("endSection") Integer endSection,
                                                         @Param("weekRange") String weekRange,
                                                         @Param("excludeId") Long excludeId);

    /**
     * 检测班级时间冲突
     * @param semesterId 学期ID
     * @param classIds 班级ID列表
     * @param weekDay 星期几
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @param weekRange 周次范围
     * @param excludeId 排除的排课ID（更新时使用）
     * @return
     */
    List<CourseScheduleDetailVO> checkClassConflict(@Param("semesterId") Long semesterId,
                                                     @Param("classIds") List<Long> classIds,
                                                     @Param("weekDay") Integer weekDay,
                                                     @Param("startSection") Integer startSection,
                                                     @Param("endSection") Integer endSection,
                                                     @Param("weekRange") String weekRange,
                                                     @Param("excludeId") Long excludeId);

    /**
     * 查询学生的课表课程（含课程元数据：credit, type等）
     * @param semesterId 学期ID
     * @param classIds 班级ID列表
     * @return
     */
    List<StudentCourseVO> getStudentCourseSchedules(@Param("semesterId") Long semesterId,
                                                     @Param("classIds") List<Long> classIds);

    /**
     * 根据学生ID获取班级ID列表
     * @param studentId 学生ID
     * @return 班级ID列表
     */
    List<Long> getClassIdsByStudentId(@Param("studentId") Long studentId);

    /**
     * 插入排课班级关联
     */
    int insertCourseScheduleClasses(@Param("scheduleId") Long scheduleId, @Param("classIds") List<Long> classIds);

    /**
     * 删除排课班级关联
     */
    int deleteCourseScheduleClasses(@Param("scheduleId") Long scheduleId);

    // ============ 学生Dashboard相关 ============

    /**
     * 统计学生已选课程数
     */
    @Select("SELECT COUNT(*) FROM course_selection WHERE student_id = #{studentId} AND status = '已选'")
    Integer countStudentCourses(@Param("studentId") Long studentId);

    /**
     * 获取学生平均成绩
     */
    @Select("SELECT AVG(score) FROM course_selection WHERE student_id = #{studentId} AND score IS NOT NULL")
    BigDecimal getStudentAvgScore(@Param("studentId") Long studentId);

    /**
     * 统计学生待审批请假数
     */
    @Select("SELECT COUNT(*) FROM leave_request WHERE student_id = #{studentId} AND status = '待审批'")
    Integer countStudentLeaves(@Param("studentId") Long studentId);

    /**
     * 统计学生今日课程数
     */
    @Select("SELECT COUNT(DISTINCT cs.schedule_id) FROM course_schedule cs " +
            "LEFT JOIN course_schedule_classes csc ON cs.id = csc.schedule_id " +
            "LEFT JOIN student s ON s.class_id = csc.class_id " +
            "WHERE s.id = #{studentId} AND cs.week_day = #{weekDay}")
    Integer countTodayCourses(@Param("studentId") Long studentId, @Param("weekDay") int weekDay);
}
