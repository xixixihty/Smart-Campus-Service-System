package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.vo.*;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.MakeupExamNotificationService;
import com.hxq.smart_campus.service.MakeupExamSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 补考场次管理（管理员端）
 */
@RestController
@RequestMapping("/api/makeup-sessions/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "补考场次管理（新）")
public class MakeupSessionAdminController {

    private final MakeupExamSessionService sessionService;
    private final MakeupExamNotificationService notificationService;

    /**
     * 扫描补考资格（按课程分组）
     */
    @GetMapping("/candidates")
    @Operation(summary = "扫描补考资格")
    public Result<List<CandidateGroupVO>> scanCandidates(
            @RequestParam(required = false) Long courseId,
            @RequestParam Long semesterId) {
        log.info("扫描补考资格: courseId={}, semesterId={}", courseId, semesterId);
        List<CandidateGroupVO> result = sessionService.scanCandidates(courseId, semesterId);
        return Result.success(result);
    }

    /**
     * 创建补考场次
     */
    @PostMapping
    @Operation(summary = "创建补考场次")
    public Result<MakeupExamSessionDetailVO> createSession(@RequestBody MakeupExamSessionCreateDTO dto) {
        log.info("创建补考场次: {}", dto);
        MakeupExamSessionDetailVO result = sessionService.createSession(dto);
        return Result.success(result);
    }

    /**
     * 更新补考场次
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新补考场次")
    public Result<MakeupExamSessionDetailVO> updateSession(@PathVariable Long id,
                                                           @RequestBody MakeupExamSessionUpdateDTO dto) {
        dto.setId(id);
        log.info("更新补考场次: {}", dto);
        MakeupExamSessionDetailVO result = sessionService.updateSession(dto);
        return Result.success(result);
    }

    /**
     * 删除补考场次
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除补考场次")
    public Result<Boolean> deleteSession(@PathVariable Long id) {
        log.info("删除补考场次: {}", id);
        boolean result = sessionService.deleteSession(id);
        return Result.success(result);
    }

    /**
     * 取消补考场次
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消补考场次")
    public Result<Boolean> cancelSession(@PathVariable Long id) {
        log.info("取消补考场次: {}", id);
        boolean result = sessionService.cancelSession(id);
        if (result) {
            notificationService.notifySessionCanceled(id);
        }
        return Result.success(result);
    }

    /**
     * 分页查询场次列表
     */
    @GetMapping
    @Operation(summary = "查询补考场次列表")
    public Result<PageInfo<MakeupExamSessionListVO>> getSessionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String examDate) {
        log.info("查询补考场次列表: pageNum={}, pageSize={}, courseId={}, semesterId={}, status={}, examDate={}",
                pageNum, pageSize, courseId, semesterId, status, examDate);
        PageInfo<MakeupExamSessionListVO> result = sessionService.getSessionList(
                pageNum, pageSize, courseId, semesterId, status, examDate);
        return Result.success(result);
    }

    /**
     * 查询场次详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询补考场次详情")
    public Result<MakeupExamSessionDetailVO> getSessionDetail(@PathVariable Long id) {
        log.info("查询补考场次详情: {}", id);
        MakeupExamSessionDetailVO result = sessionService.getSessionDetail(id);
        return Result.success(result);
    }

    /**
     * 手动添加学生到场次
     */
    @PostMapping("/{id}/students/{studentId}")
    @Operation(summary = "添加学生到场次")
    public Result<Boolean> addStudent(@PathVariable Long id, @PathVariable Long studentId) {
        log.info("添加学生到场次: sessionId={}, studentId={}", id, studentId);
        sessionService.addStudentToSession(id, studentId);
        return Result.success(true);
    }

    /**
     * 从场次移除学生
     */
    @DeleteMapping("/{id}/students/{studentId}")
    @Operation(summary = "从场次移除学生")
    public Result<Boolean> removeStudent(@PathVariable Long id, @PathVariable Long studentId) {
        log.info("从场次移除学生: sessionId={}, studentId={}", id, studentId);
        sessionService.removeStudentFromSession(id, studentId);
        return Result.success(true);
    }

    /**
     * 批量通知学生
     */
    @PostMapping("/{id}/notify")
    @Operation(summary = "通知补考学生")
    public Result<Boolean> notifyStudents(@PathVariable Long id) {
        log.info("通知补考学生: sessionId={}", id);
        notificationService.notifySessionArranged(id);
        return Result.success(true);
    }

    /**
     * 批量录入补考成绩
     */
    @PutMapping("/{id}/scores")
    @Operation(summary = "批量录入补考成绩")
    public Result<Boolean> batchInputScore(@PathVariable Long id, @RequestBody BatchMakeupScoreDTO dto) {
        dto.setSessionId(id);
        log.info("批量录入补考成绩: sessionId={}, count={}", id,
                dto.getScores() != null ? dto.getScores().size() : 0);
        sessionService.batchInputScore(dto);
        // 异步发送成绩通知
        notificationService.notifyScorePublished(id);
        return Result.success(true);
    }

    /**
     * 录入单个学生补考成绩
     */
    @PutMapping("/{id}/students/{studentId}/score")
    @Operation(summary = "录入单个学生补考成绩")
    public Result<Boolean> inputSingleScore(@PathVariable Long id,
                                            @PathVariable Long studentId,
                                            @RequestBody MakeupScoreInputDTO dto) {
        dto.setSessionId(id);
        dto.setStudentId(studentId);
        log.info("录入单个学生补考成绩: sessionId={}, studentId={}, score={}", id, studentId, dto.getScore());
        sessionService.inputSingleScore(dto);
        return Result.success(true);
    }
}