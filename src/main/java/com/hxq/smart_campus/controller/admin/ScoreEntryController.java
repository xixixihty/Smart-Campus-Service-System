package com.hxq.smart_campus.controller.admin;

import com.github.pagehelper.PageInfo;
import com.hxq.smart_campus.entity.dto.*;
import com.hxq.smart_campus.entity.vo.GpaVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryDetailVO;
import com.hxq.smart_campus.entity.vo.ScoreEntryListVO;
import com.hxq.smart_campus.entity.vo.ScoreStatisticsVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ScoreEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/score-entries/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "成绩管理模块")
public class ScoreEntryController {
    private final ScoreEntryService scoreEntryService;

    /**
     * 录入成绩
     * @param scoreEntryCreateDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "录入成绩")
    public Result<ScoreEntryResponseDTO> insertScore(@RequestBody ScoreEntryCreateDTO scoreEntryCreateDTO) {
        log.info("录入成绩: {}", scoreEntryCreateDTO);
        ScoreEntryResponseDTO scoreEntryResponseDTO = scoreEntryService.insertScore(scoreEntryCreateDTO);
        return Result.success(scoreEntryResponseDTO);
    }

    /**
     * 更新成绩
     * @param scoreEntryUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "更新成绩")
    public Result<ScoreEntryResponseDTO> updateScore(@RequestBody ScoreEntryUpdateDTO scoreEntryUpdateDTO) {
        log.info("更新成绩: {}", scoreEntryUpdateDTO);
        ScoreEntryResponseDTO scoreEntryResponseDTO = scoreEntryService.updateScore(scoreEntryUpdateDTO);
        return Result.success(scoreEntryResponseDTO);
    }

    /**
     * 批量录入成绩
     * @param batchScoreEntryDTO
     * @return
     */
    @PostMapping("/batch")
    @Operation(summary = "批量录入成绩")
    public Result<Boolean> batchScore(@RequestBody BatchScoreEntryDTO batchScoreEntryDTO) {
        log.info("批量录入成绩: {}", batchScoreEntryDTO);
        boolean result = scoreEntryService.batchScore(batchScoreEntryDTO);
        return Result.success(result);
    }

    /**
     * 获取成绩列表
     * @param pageNum
     * @param pageSize
     * @param courseId
     * @param studentId
     * @param semesterId
     * @return
     */
    @GetMapping
    @Operation(summary = "获取成绩列表")
    public Result<PageInfo<ScoreEntryListVO>> getScoreList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long semesterId
    ) {
        log.info("获取成绩列表: pageNum={}, pageSize={}, courseId={}, studentId={}, semesterId={}", pageNum, pageSize, courseId, studentId, semesterId);
        PageInfo<ScoreEntryListVO> scoreList = scoreEntryService.getScoreList(pageNum, pageSize, courseId, studentId, semesterId);
        return Result.success(scoreList);
    }

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

//    /**
//     * 获取我的成绩列表
//     * @param semesterId
//     * @param courseId
//     * @return
//     */
//    @GetMapping("/my")
//    @Operation(summary = "获取我的成绩列表")
//    public Result<List<ScoreEntryListVO>> getMyScoreList(
//            @RequestParam(required = false) Long semesterId,
//            @RequestParam(required = false) Long courseId
//    ) {
//        log.info("获取我的成绩列表: semesterId={}, courseId={}", semesterId, courseId);
//        List<ScoreEntryListVO> scoreList = scoreEntryService.getMyScoreList(semesterId, courseId);
//        return Result.success(scoreList);
//    }

    /**
     * 获取成绩统计信息
     * @param semesterId
     * @param dimension
     * @param courseId
     * @param studentId
     * @param classId
     * @param majorId
     * @param collegeId
     * @return
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取成绩统计")
    public Result<ScoreStatisticsVO> getScoreStatistics(
            @Parameter(description = "学期ID", required = true) @RequestParam Long semesterId,
            @Parameter(description = "统计维度", required = true) @RequestParam StatisticsDimensionEnum dimension,
            @Parameter(description = "课程ID（维度为COURSE时必填）") @RequestParam(required = false) Long courseId,
            @Parameter(description = "学生ID（维度为STUDENT时必填）") @RequestParam(required = false) Long studentId,
            @Parameter(description = "班级ID（维度为CLASS时必填）") @RequestParam(required = false) Long classId,
            @Parameter(description = "专业ID（维度为MAJOR时必填）") @RequestParam(required = false) Long majorId,
            @Parameter(description = "学院ID（维度为COLLEGE时必填）") @RequestParam(required = false) Long collegeId
    ) {
        log.info("获取成绩统计: semesterId={}, dimension={}, courseId={}, studentId={}, classId={}, majorId={}, collegeId={}",
                semesterId, dimension, courseId, studentId, classId, majorId, collegeId);

        ScoreStatisticsQueryDTO queryDTO = new ScoreStatisticsQueryDTO();
        queryDTO.setSemesterId(semesterId);
        queryDTO.setDimension(dimension);
        queryDTO.setCourseId(courseId);
        queryDTO.setStudentId(studentId);
        queryDTO.setClassId(classId);
        queryDTO.setMajorId(majorId);
        queryDTO.setCollegeId(collegeId);

        ScoreStatisticsVO scoreStatisticsVO = scoreEntryService.getScoreStatistics(queryDTO);
        return Result.success(scoreStatisticsVO);
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
        GpaVO gpaVO = scoreEntryService.getGpa(studentId, semesterId);
        return Result.success(gpaVO);
    }
}