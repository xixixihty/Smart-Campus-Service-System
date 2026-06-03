package com.hxq.smart_campus.controller.teacher;

import com.hxq.smart_campus.entity.vo.CourseDetailVO;
import com.hxq.smart_campus.entity.vo.CourseListVO;
import com.hxq.smart_campus.result.Result;
import com.hxq.smart_campus.service.CourseService;
import com.hxq.smart_campus.service.SemesterService;
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
@Tag(name = "教师课程查看模块")
@RequiredArgsConstructor
@Validated
public class TeacherCourseController {

    private final CourseService courseService;
    private final SemesterService semesterService;

    @GetMapping("/courses")
    @Operation(summary = "查看所授课程列表")
    public Result<List<CourseListVO>> getTeachingCourses(@RequestParam(required = false) Long semesterId) {
        var currentSemester = semesterService.getCurrentSemester();
        Long currentSemesterId = currentSemester.getId();
        log.info("教师查询所授课程: 前端传入semesterId={}, 强制使用当前学期: {} (ID={})",
                semesterId, currentSemester.getName(), currentSemesterId);
        List<CourseListVO> courses = courseService.getTeachingCourses(currentSemesterId);
        return Result.success(courses);
    }

    @GetMapping("/course/{id}")
    @Operation(summary = "查看课程详情")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long id) {
        log.info("教师查询课程详情: id={}", id);
        CourseDetailVO detail = courseService.getCourseDetail(id);
        return Result.success(detail);
    }
}