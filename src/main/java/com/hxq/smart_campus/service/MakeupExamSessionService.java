package com.hxq.smart_campus.service;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.vo.*;

import java.util.List;

/**
 * 补考场次服务接口
 */
public interface MakeupExamSessionService {

    /**
     * 扫描补考资格（按课程+学期获取待补考学生）
     */
    List<CandidateGroupVO> scanCandidates(Long courseId, Long semesterId);

    /**
     * 创建补考场次（含自动分配学生）
     */
    MakeupExamSessionDetailVO createSession(MakeupExamSessionCreateDTO dto);

    /**
     * 更新补考场次
     */
    MakeupExamSessionDetailVO updateSession(MakeupExamSessionUpdateDTO dto);

    /**
     * 删除补考场次（仅待安排状态）
     */
    boolean deleteSession(Long id);

    /**
     * 取消补考场次
     */
    boolean cancelSession(Long id);

    /**
     * 分页查询场次列表
     */
    PageInfo<MakeupExamSessionListVO> getSessionList(Integer pageNum, Integer pageSize,
                                                     Long courseId, Long semesterId,
                                                     String status, String examDate);

    /**
     * 查询场次详情（含学生列表）
     */
    MakeupExamSessionDetailVO getSessionDetail(Long id);

    /**
     * 手动添加学生到场次
     */
    void addStudentToSession(Long sessionId, Long studentId);

    /**
     * 从场次移除学生
     */
    void removeStudentFromSession(Long sessionId, Long studentId);

    /**
     * 批量录入补考成绩
     */
    void batchInputScore(BatchMakeupScoreDTO dto);

    /**
     * 录入单个学生补考成绩
     */
    void inputSingleScore(MakeupScoreInputDTO dto);

    /**
     * 学生端：查询我的补考列表
     */
    List<MyMakeupSessionVO> getMySessions(String status);
}