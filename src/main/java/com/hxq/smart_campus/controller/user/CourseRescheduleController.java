package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.dto.CourseRescheduleCreateDTO;
import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseRescheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-reschedule")
@RequiredArgsConstructor
@Tag(name = "调课管理模块")
@Slf4j
public class CourseRescheduleController {

    private final CourseRescheduleService courseRescheduleService;

    @PostMapping
    @Operation(summary = "创建调课申请")
    public Result<List<CourseRescheduleVO>> createReschedule(@RequestBody CourseRescheduleCreateDTO dto) {
        log.info("创建调课申请：{}", dto);
        List<CourseRescheduleVO> result = courseRescheduleService.createReschedule(dto);
        return Result.success(result);
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认调课")
    public Result<Void> confirmReschedule(@PathVariable Long id) {
        log.info("确认调课：{}", id);
        courseRescheduleService.confirmReschedule(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消调课")
    public Result<Void> cancelReschedule(@PathVariable Long id) {
        log.info("取消调课：{}", id);
        courseRescheduleService.cancelReschedule(id);
        return Result.success();
    }

    @GetMapping("/leave/{leaveRequestId}")
    @Operation(summary = "查询请假关联的调课列表")
    public Result<List<CourseRescheduleVO>> getRescheduleListByLeaveRequest(@PathVariable Long leaveRequestId) {
        log.info("查询请假关联的调课列表：{}", leaveRequestId);
        List<CourseRescheduleVO> list = courseRescheduleService.getRescheduleListByLeaveRequest(leaveRequestId);
        return Result.success(list);
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的调课列表")
    public Result<List<CourseRescheduleVO>> getRescheduleListByTeacher() {
        log.info("查询我的调课列表");
        List<CourseRescheduleVO> list = courseRescheduleService.getRescheduleListByTeacher();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询调课详情")
    public Result<CourseRescheduleVO> getRescheduleDetail(@PathVariable Long id) {
        log.info("查询调课详情：{}", id);
        CourseRescheduleVO vo = courseRescheduleService.getRescheduleDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/schedules")
    @Operation(summary = "查询教师课表")
    public Result<List<CourseRescheduleVO>> getTeacherCourseSchedules() {
        log.info("查询教师课表");
        List<CourseRescheduleVO> list = courseRescheduleService.getTeacherCourseSchedules();
        return Result.success(list);
    }
}