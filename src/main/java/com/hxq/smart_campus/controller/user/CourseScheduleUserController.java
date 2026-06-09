package com.hxq.smart_campus.controller.user;

import com.hxq.smart_campus.entity.vo.StudentCourseVO;
import com.hxq.smart_campus.entity.vo.StudentDashboardVO;
import com.hxq.smart_campus.entity.vo.TimetableVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseScheduleService;
import com.hxq.smart_campus.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course-schedules/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户课表查询模块")
public class CourseScheduleUserController {
    private final CourseScheduleService courseScheduleService;

    /**
     * 获取课表
     * @param semesterId
     * @param userId
     * @param userType
     * @return
     */
    @GetMapping("/timetable")
    @Operation(summary = "获取课表")
    public Result<List<TimetableVO>> getTimetable(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userType
    ) {
        if (userId == null) {
            userId = SecurityUtils.getCurrentUserId();
        }
        if (userType == null) {
            userType = SecurityUtils.getCurrentUserType();
        }
        log.info("获取课表: semesterId={}, userId={}, userType={}", semesterId, userId, userType);
        List<TimetableVO> result = courseScheduleService.queryTimetable(semesterId, userId, userType);
        return Result.success(result);
    }

    /**
     * 获取学生的全部课程（课表课程 + 选课课程合并去重）
     * @param semesterId 学期ID
     * @param studentId 学生ID
     * @return
     */
    @GetMapping("/my-courses")
    @Operation(summary = "获取我的全部课程")
    public Result<List<StudentCourseVO>> getMyAllCourses(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long studentId
    ) {
        if (studentId == null) {
            studentId = SecurityUtils.getCurrentUserId();
        }
        log.info("获取我的全部课程: semesterId={}, studentId={}", semesterId, studentId);
        List<StudentCourseVO> result = courseScheduleService.getStudentAllCourses(semesterId, studentId);
        return Result.success(result);
    }

    /**
     * 获取学生工作台统计数据
     * @return
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取学生工作台统计数据")
    public Result<StudentDashboardVO> getDashboardStats() {
        Long studentId = SecurityUtils.getCurrentUserId();
        log.info("获取学生工作台统计数据: studentId={}", studentId);
        StudentDashboardVO dashboard = courseScheduleService.getStudentDashboardStats(studentId);
        return Result.success(dashboard);
    }
}
