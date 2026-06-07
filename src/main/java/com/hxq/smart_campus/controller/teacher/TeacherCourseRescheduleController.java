package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.dto.CourseRescheduleCreateDTO;
import com.hxq.smart_campus.entity.vo.ClassroomListVO;
import com.hxq.smart_campus.entity.vo.CourseRescheduleVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.ClassroomService;
import com.hxq.smart_campus.service.CourseRescheduleService;
import com.github.pagehelper.PageInfo;
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
@Tag(name = "教师调课管理模块")
@RequiredArgsConstructor
@Validated
public class TeacherCourseRescheduleController {

    private final CourseRescheduleService courseRescheduleService;
    private final ClassroomService classroomService;

    @PostMapping("/reschedule")
    @Operation(summary = "创建调课申请")
    public Result<List<CourseRescheduleVO>> createReschedule(@Valid @RequestBody CourseRescheduleCreateDTO dto) {
        log.info("教师创建调课申请: reason={}, itemCount={}", dto.getReason(),
                dto.getItems() != null ? dto.getItems().size() : 0);
        List<CourseRescheduleVO> result = courseRescheduleService.createReschedule(dto);
        return Result.success(result);
    }

    @GetMapping("/reschedules")
    @Operation(summary = "查看我的调课记录列表")
    public Result<List<CourseRescheduleVO>> getMyReschedules() {
        log.info("教师查询个人调课记录");
        List<CourseRescheduleVO> list = courseRescheduleService.getRescheduleListByTeacher();
        return Result.success(list);
    }

    @GetMapping("/reschedule/{id}")
    @Operation(summary = "查看调课详情")
    public Result<CourseRescheduleVO> getRescheduleDetail(@PathVariable Long id) {
        log.info("教师查看调课详情: id={}", id);
        CourseRescheduleVO detail = courseRescheduleService.getRescheduleDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/reschedules/teaching")
    @Operation(summary = "查看授课排课记录（用于调课参考）")
    public Result<List<CourseRescheduleVO>> getTeachingCourseSchedules() {
        log.info("教师查询授课排课记录");
        List<CourseRescheduleVO> list = courseRescheduleService.getTeacherCourseSchedules();
        return Result.success(list);
    }

    @PutMapping("/reschedule/{id}/cancel")
    @Operation(summary = "取消调课申请")
    public Result<?> cancelReschedule(@PathVariable Long id) {
        log.info("教师取消调课: id={}", id);
        courseRescheduleService.cancelReschedule(id);
        return Result.success();
    }

    @GetMapping("/classrooms")
    @Operation(summary = "获取教室列表（用于调课选择）")
    public Result<PageInfo<ClassroomListVO>> getClassrooms(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "200") Integer pageSize) {
        PageInfo<ClassroomListVO> pageInfo = classroomService.getClassroomList(pageNum, pageSize, null, null, null);
        return Result.success(pageInfo);
    }
}