package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.vo.MakeupExamCandidateVO;
import com.hxq.smart_campus.entity.vo.MakeupExamStudentVO;
import com.hxq.smart_campus.entity.vo.MyMakeupSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 补考学生关联 Mapper
 */
@Mapper
public interface MakeupExamStudentMapper {

    /**
     * 批量插入学生关联
     */
    int batchInsert(@Param("list") List<com.hxq.smart_campus.entity.pojo.MakeupExamStudent> list);

    /**
     * 删除指定学生关联
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM makeup_exam_student WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    int deleteBySessionAndStudent(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * 查询某场次的所有学生列表
     */
    List<MakeupExamStudentVO> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 更新学生成绩和状态
     */
    @Update("UPDATE makeup_exam_student SET makeup_score = #{score}, student_status = #{status} WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    int updateScore(@Param("sessionId") Long sessionId,
                    @Param("studentId") Long studentId,
                    @Param("score") BigDecimal score,
                    @Param("status") String status);

    /**
     * 批量更新学生状态
     */
    @Update("UPDATE makeup_exam_student SET student_status = #{newStatus}, notification_time = NOW() WHERE session_id = #{sessionId} AND student_status = #{oldStatus}")
    int batchUpdateStatus(@Param("sessionId") Long sessionId,
                          @Param("oldStatus") String oldStatus,
                          @Param("newStatus") String newStatus);

    /**
     * 统计未录入成绩的学生数
     */
    @Select("SELECT COUNT(*) FROM makeup_exam_student WHERE session_id = #{sessionId} AND makeup_score IS NULL")
    int countUnscored(@Param("sessionId") Long sessionId);

    /**
     * 检查学生时间冲突
     */
    List<MyMakeupSessionVO> checkStudentTimeConflict(@Param("studentId") Long studentId,
                                                      @Param("examDate") LocalDate examDate,
                                                      @Param("examStartTime") LocalTime examStartTime,
                                                      @Param("examEndTime") LocalTime examEndTime);

    /**
     * 扫描补考资格（按课程+学期）
     */
    List<MakeupExamCandidateVO> scanCandidates(@Param("courseId") Long courseId,
                                               @Param("semesterId") Long semesterId);

    /**
     * 学生端：查询我的补考列表
     */
    List<MyMakeupSessionVO> selectMySessions(@Param("studentId") Long studentId,
                                             @Param("status") String status);
}