package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.vo.GpaVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryDetailVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ScoreEntryService;
import com.hxq.smart_campus.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score-entries/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户成绩查询模块")
public class ScoreEntryUserController {
    private final ScoreEntryService scoreEntryService;

    /**
     * 获取成绩详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取成绩详情")
    public Result<ScoreEntryDetailVO> getScoreDetail(@PathVariable Long id) {
        log.info("获取成绩详情: id={}", id);
        ScoreEntryDetailVO scoreEntryDetailVO = scoreEntryService.getScoreDetail(id);
        return Result.success(scoreEntryDetailVO);
    }

    /**
     * 获取我的成绩列表
     * @param semesterId
     * @param courseId
     * @return
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的成绩列表")
    public Result<List<ScoreEntryListVO>> getMyScoreList(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId
    ) {
        log.info("获取我的成绩列表: semesterId={}, courseId={}", semesterId, courseId);
        List<ScoreEntryListVO> scoreList = scoreEntryService.getMyScoreList(semesterId, courseId);
        return Result.success(scoreList);
    }

    /**
     * 获取平均绩点
     * @param studentId
     * @param semesterId
     * @return
     */
    @GetMapping("/gpa")
    @Operation(summary = "获取GPA")
    public Result<GpaVO> getGpa(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long semesterId
    ) {
        log.info("获取GPA: studentId={}, semesterId={}", studentId, semesterId);
        // 从登陆信息中获取到用户信息
        studentId = SecurityUtils.getCurrentUserId();
        GpaVO gpaVO = scoreEntryService.getGpa(studentId, semesterId);
        return Result.success(gpaVO);
    }
}
