package com.hxq.smart_campus.mapper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成绩录入 Mapper 接口
 */
@Mapper
public interface ScoreEntryMapper {

    /**
     * 录入成绩
     *
     * @param scoreEntryCreateDTO 成绩录入参数
     * @return 影响行数
     */
    int insertScore(ScoreEntryCreateDTO scoreEntryCreateDTO);

    /**
     * 获取最后插入的ID
     *
     * @return 最后插入的ID
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 获取成绩详情
     *
     * @param id 成绩ID
     * @return 成绩详情VO
     */
    ScoreEntryDetailVO getScoreDetail(Long id);

    /**
     * 更新成绩
     *
     * @param scoreEntryUpdateDTO 成绩更新参数
     * @return 影响行数
     */
    int updateScore(ScoreEntryUpdateDTO scoreEntryUpdateDTO);

    /**
     * 获取成绩列表
     *
     * @param courseId   课程ID
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 成绩列表
     */
    List<ScoreEntryListVO> getScoreList(@Param("courseId") Long courseId,
                                        @Param("studentId") Long studentId,
                                        @Param("semesterId") Long semesterId);

    List<ScoreEntryListVO> getScoreListByClassId(@Param("classId") Long classId,
                                                  @Param("semesterId") Long semesterId);

    /**
     * 根据维度查询成绩列表（用于统计分析）
     * Mapper层只做一次查询，Service层负责内存统计计算
     *
     * @param queryDTO 查询参数（包含学期ID、统计维度及对应维度ID）
     * @return 成绩列表
     */
    List<ScoreEntryForStatisticsVO> getScoreListForStatistics(@Param("query") ScoreStatisticsQueryDTO queryDTO);

    /**
     * 根据学生ID获取学生姓名
     *
     * @param studentId 学生ID
     * @return 学生姓名
     */
    @Select("SELECT name FROM student WHERE id = #{studentId}")
    String getStudentNameById(@Param("studentId") Long studentId);

    /**
     * 根据学生ID获取学号
     *
     * @param studentId 学生ID
     * @return 学号
     */
    @Select("SELECT student_no FROM student WHERE id = #{studentId}")
    String getStudentNoById(@Param("studentId") Long studentId);

    /**
     * 根据课程ID获取课程名称
     *
     * @param courseId 课程ID
     * @return 课程名称
     */
    @Select("SELECT course_name FROM course WHERE id = #{courseId}")
    String getCourseNameById(@Param("courseId") Long courseId);

    /**
     * 根据班级ID获取班级名称
     *
     * @param classId 班级ID
     * @return 班级名称
     */
    @Select("SELECT class_name FROM class WHERE id = #{classId}")
    String getClassNameById(@Param("classId") Long classId);

    /**
     * 根据专业ID获取专业名称
     *
     * @param majorId 专业ID
     * @return 专业名称
     */
    @Select("SELECT major_name FROM major WHERE id = #{majorId}")
    String getMajorNameById(@Param("majorId") Long majorId);

    /**
     * 根据学院ID获取学院名称
     *
     * @param collegeId 学院ID
     * @return 学院名称
     */
    @Select("SELECT college_name FROM college WHERE id = #{collegeId}")
    String getCollegeNameById(@Param("collegeId") Long collegeId);

    /**
     * 根据学期ID获取学期名称
     *
     * @param semesterId 学期ID
     * @return 学期名称
     */
    @Select("SELECT name FROM semester WHERE id = #{semesterId}")
    String getSemesterNameById(@Param("semesterId") Long semesterId);

    /**
     * 批量录入成绩
     * @param scoreEntries
     * @return
     */
    int batchInsertScore(@Param("scoreEntries") List<ScoreEntryCreateDTO> scoreEntries);

    List<ScoreEntryListVO> getUnrecordedStudents(@Param("courseId") Long courseId, @Param("semesterId") Long semesterId);

    TeacherScoreStatsDTO getTeacherScoreStats(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    List<ScoreDistributionItem> getScoreDistributionByTeacher(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    /**
     * 校验教师对指定课程+学期的成绩操作权限
     * @return 1=有权限, 0=无权限
     */
    @Select("SELECT COUNT(1) FROM course_schedule " +
            "WHERE teacher_id = #{teacherId} " +
            "AND course_id = #{courseId} " +
            "AND semester_id = #{semesterId}")
    int checkTeacherCoursePermission(@Param("teacherId") Long teacherId,
                                      @Param("courseId") Long courseId,
                                      @Param("semesterId") Long semesterId);
}
