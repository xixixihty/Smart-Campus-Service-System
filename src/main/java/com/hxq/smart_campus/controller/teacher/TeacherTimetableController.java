package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.vo.TimetableVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseScheduleService;
import com.hxq.smart_campus.service.SemesterService;
import com.hxq.smart_campus.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@Slf4j
@Tag(name = "教师课表模块")
@RequiredArgsConstructor
@Validated
public class TeacherTimetableController {

    private final CourseScheduleService courseScheduleService;
    private final SemesterService semesterService;

    @GetMapping("/timetable")
    @Operation(summary = "查看个人课表")
    public Result<List<TimetableVO>> getMyTimetable(@RequestParam(required = false) Long semesterId) {
        Long userId = SecurityUtils.getCurrentUserId();
        var currentSemester = semesterService.getCurrentSemester();
        Long currentSemesterId = currentSemester.getId();
        log.info("教师查询个人课表: teacherId={}, 前端传入semesterId={}, 强制使用当前学期: {} (ID={})",
                userId, semesterId, currentSemester.getName(), currentSemesterId);
        List<TimetableVO> timetables = courseScheduleService.queryTimetable(currentSemesterId, userId, "teacher");
        return Result.success(timetables);
    }

    @GetMapping("/teaching-schedules")
    @Operation(summary = "查询指定学期的排课记录")
    public Result<List<TimetableVO>> getTeachingSchedules(@RequestParam(required = false) Long semesterId, @RequestParam(required = false) Integer weekDay) {
        Long userId = SecurityUtils.getCurrentUserId();
        var currentSemester = semesterService.getCurrentSemester();
        Long currentSemesterId = currentSemester.getId();
        log.info("教师查询排课记录: teacherId={}, 前端传入semesterId={}, weekDay={}, 强制使用当前学期: {} (ID={})",
                userId, semesterId, weekDay, currentSemester.getName(), currentSemesterId);
        List<TimetableVO> timetables = courseScheduleService.queryTimetable(currentSemesterId, userId, "teacher");
        if (weekDay != null) {
            timetables.removeIf(t -> !t.getWeekDay().equals(weekDay));
        }
        return Result.success(timetables);
    }
}