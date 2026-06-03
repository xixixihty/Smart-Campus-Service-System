package com.hxq.smart_campus.controller.teacher;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.ScoreEntryCreateDTO;
import com.hxq.smart_campus.entity.dto.ScoreEntryUpdateDTO;
import com.hxq.smart_campus.entity.dto.BatchScoreEntryDTO;
import com.hxq.smart_campus.entity.dto.ScoreEntryResponseDTO;
import com.hxq.smart_campus.entity.vo.ScoreEntryDetailVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.entity.vo.TeacherScoreStatsDTO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ScoreEntryService;
import com.hxq.smart_campus.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师成绩管理模块")
@RequiredArgsConstructor
@Validated
public class TeacherScoreController {

    private final ScoreEntryService scoreEntryService;
    private final SemesterService semesterService;

    @PostMapping("/score")
    @Operation(summary = "录入单个学生成绩")
    public Result<ScoreEntryResponseDTO> insertScore(@Valid @RequestBody ScoreEntryCreateDTO dto) {
        log.info("教师录入成绩: courseId={}, studentId={}", dto.getCourseId(), dto.getStudentId());
        ScoreEntryResponseDTO result = scoreEntryService.insertScore(dto);
        return Result.success(result);
    }

    @PostMapping("/scores/batch")
    @Operation(summary = "批量录入成绩")
    public Result<?> batchInsertScore(@Valid @RequestBody BatchScoreEntryDTO dto) {
        log.info("教师批量录入成绩: courseId={}, count={}", dto.getCourseId(), dto.getScoreEntries().size());
        scoreEntryService.batchScore(dto);
        return Result.success();
    }

    @PutMapping("/score")
    @Operation(summary = "修改成绩（含补考成绩）")
    public Result<ScoreEntryResponseDTO> updateScore(@Valid @RequestBody ScoreEntryUpdateDTO dto) {
        log.info("教师修改成绩: id={}", dto.getId());
        ScoreEntryResponseDTO result = scoreEntryService.updateScore(dto);
        return Result.success(result);
    }

    @GetMapping("/scores")
    @Operation(summary = "查询成绩列表（按课程+学期）")
    public Result<PageInfo<ScoreEntryListVO>> getScoreList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long courseId,
            @RequestParam Long semesterId) {
        log.info("教师查询成绩列表: courseId={}, semesterId={}", courseId, semesterId);
        PageInfo<ScoreEntryListVO> list = scoreEntryService.getScoreList(pageNum, pageSize, courseId, null, semesterId);
        return Result.success(list);
    }

    @GetMapping("/score/{id}")
    @Operation(summary = "查看成绩详情")
    public Result<ScoreEntryDetailVO> getScoreDetail(@PathVariable Long id) {
        log.info("教师查询成绩详情: id={}", id);
        ScoreEntryDetailVO detail = scoreEntryService.getScoreDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/scores/unrecorded")
    @Operation(summary = "获取未录入成绩的学生列表")
    public Result<List<ScoreEntryListVO>> getUnrecordedStudents(
            @RequestParam Long courseId,
            @RequestParam Long semesterId) {
        log.info("教师查询未录入成绩学生: courseId={}, semesterId={}", courseId, semesterId);
        List<ScoreEntryListVO> students = scoreEntryService.getUnrecordedStudents(courseId, semesterId);
        return Result.success(students);
    }

    @GetMapping("/scores/stats")
    @Operation(summary = "获取成绩统计信息")
    public Result<TeacherScoreStatsDTO> getScoreStats(@RequestParam(required = false) Long semesterId) {
        var currentSemester = semesterService.getCurrentSemester();
        Long currentSemesterId = currentSemester.getId();
        log.info("教师查询成绩统计: 前端传入semesterId={}, 强制使用当前学期: {} (ID={})",
                semesterId, currentSemester.getName(), currentSemesterId);
        TeacherScoreStatsDTO stats = scoreEntryService.getTeacherScoreStats(currentSemesterId);
        return Result.success(stats);
    }
}