package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.vo.SemesterDetailVO;
import com.hxq.smart_campus.entity.vo.SemesterListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.github.pagehelper.PageInfo;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师学期查看模块")
@RequiredArgsConstructor
@Validated
public class TeacherSemesterController {

    private final SemesterService semesterService;

    @GetMapping("/semesters")
    @Operation(summary = "查看学期列表")
    public Result<PageInfo<SemesterListVO>> getSemesterList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("教师查询学期列表: pageNum={}, pageSize={}", pageNum, pageSize);
        PageInfo<SemesterListVO> list = semesterService.getSemesterList(pageNum, pageSize, null, null);
        return Result.success(list);
    }

    @GetMapping("/semester/current")
    @Operation(summary = "查看当前学期")
    public Result<SemesterDetailVO> getCurrentSemester() {
        SemesterDetailVO current = semesterService.getCurrentSemester();
        return Result.success(current);
    }

    @GetMapping("/semester/{id}")
    @Operation(summary = "查看学期详情")
    public Result<SemesterDetailVO> getSemesterDetail(@PathVariable Long id) {
        log.info("教师查询学期详情: id={}", id);
        SemesterDetailVO detail = semesterService.getSemesterDetail(id);
        return Result.success(detail);
    }
}