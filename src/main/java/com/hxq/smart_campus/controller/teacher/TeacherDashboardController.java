package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.vo.TeacherDashboardVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.SemesterService;
import com.hxq.smart_campus.service.TeacherDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师工作台模块")
@RequiredArgsConstructor
@Validated
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;
    private final SemesterService semesterService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取教师工作台统计数据")
    public Result<TeacherDashboardVO> getDashboardStats(@RequestParam(required = false) Long semesterId) {
        if (semesterId == null) {
            var currentSemester = semesterService.getCurrentSemester();
            semesterId = currentSemester.getId();
            log.info("教师查询工作台统计: 未传semesterId, 使用当前学期: {} (ID={})",
                    currentSemester.getName(), semesterId);
        } else {
            log.info("教师查询工作台统计: 使用指定学期ID={}", semesterId);
        }
        TeacherDashboardVO dashboard = teacherDashboardService.getTeacherDashboardStats(semesterId);
        return Result.success(dashboard);
    }
}