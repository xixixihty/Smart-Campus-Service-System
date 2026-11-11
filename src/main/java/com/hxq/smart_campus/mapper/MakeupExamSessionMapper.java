package com.hxq.smart_campus.mapper;

import com.hxq.smart_campus.entity.dto.MakeupExamSessionCreateDTO;
import com.hxq.smart_campus.entity.dto.MakeupExamSessionUpdateDTO;
import com.hxq.smart_campus.entity.vo.MakeupExamSessionDetailVO;
import com.hxq.smart_campus.entity.vo.MakeupExamSessionListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 补考场次 Mapper
 */
@Mapper
public interface MakeupExamSessionMapper {

    /**
     * 新增补考场次
     */
    int insert(MakeupExamSessionCreateDTO dto);

    /**
     * 获取最后插入的ID
     */
    @Select("SELECT LAST_INSERT_ID()")
    Long getLastInsertId();

    /**
     * 更新补考场次
     */
    int update(MakeupExamSessionUpdateDTO dto);

    /**
     * 更新场次状态
     */
    @Update("UPDATE makeup_exam_session SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新已分配人数
     */
    @Update("UPDATE makeup_exam_session SET enrolled_count = #{count} WHERE id = #{id}")
    int updateEnrolledCount(@Param("id") Long id, @Param("count") int count);

    /**
     * 删除补考场次
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM makeup_exam_session WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 查询场次详情（含课程和学期信息）
     */
    MakeupExamSessionDetailVO selectDetailById(Long id);

    /**
     * 分页查询场次列表
     */
    List<MakeupExamSessionListVO> selectList(@Param("courseId") Long courseId,
                                             @Param("semesterId") Long semesterId,
                                             @Param("status") String status,
                                             @Param("examDate") LocalDate examDate);

    /**
     * 检查同课程+学期是否已有非终态场次
     */
    @Select("SELECT COUNT(*) FROM makeup_exam_session WHERE course_id = #{courseId} AND semester_id = #{semesterId} AND status NOT IN ('已取消', '已结束')")
    int countActiveByCourseAndSemester(@Param("courseId") Long courseId, @Param("semesterId") Long semesterId);

    /**
     * 检查教室时间冲突
     */
    List<MakeupExamSessionListVO> checkClassroomConflict(@Param("location") String location,
                                                         @Param("examDate") LocalDate examDate,
                                                         @Param("examStartTime") LocalTime examStartTime,
                                                         @Param("examEndTime") LocalTime examEndTime,
                                                         @Param("excludeId") Long excludeId);

    /**
     * 查询明天有考试的已通知场次（定时任务用）
     */
    @Select("SELECT * FROM makeup_exam_session WHERE exam_date = #{examDate} AND status = '已通知'")
    List<com.hxq.smart_campus.entity.pojo.MakeupExamSession> selectByExamDateAndStatus(@Param("examDate") LocalDate examDate);
}